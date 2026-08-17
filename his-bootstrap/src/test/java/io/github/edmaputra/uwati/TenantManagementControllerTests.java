package io.github.edmaputra.uwati;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import io.github.edmaputra.uwati.bootstrap.UwatiApplication;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = UwatiApplication.class)
class TenantManagementControllerTests {

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private MockMvc mockMvc;

	@BeforeEach
	void cleanDatabase() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		jdbcTemplate.update("delete from tenant_audit_entries");
		jdbcTemplate.update("delete from tenant_document_sequences");
		jdbcTemplate.update("delete from tenant_settings");
		jdbcTemplate.update("delete from tenants");
	}

	@Test
	void createsATenantAndBootstrapsDefaultsWithoutRequiringATenantHeader() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/platform/tenants")
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "legalName": "Uwati Health Services Ltd.",
						  "displayName": "Uwati Health"
						}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.displayName").value("Uwati Health"))
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andReturn();

		String tenantId = extractField(result.getResponse().getContentAsString(), "id");
		assertThat(UUID.fromString(tenantId)).isNotNull();
		assertThat(countRows("tenants")).isEqualTo(1);
		assertThat(countRows("tenant_settings")).isEqualTo(5);
		assertThat(countRows("tenant_document_sequences")).isEqualTo(5);
		assertThat(countRows("tenant_audit_entries")).isEqualTo(1);
	}

	@Test
	void returnsTheExistingTenantForAnIdempotentProvisioningRetry() throws Exception {
		String firstTenantId = createTenant("Uwati Health Services Ltd.", "Uwati Health");
		String secondTenantId = createTenant("Uwati Health Services Ltd.", "Uwati Health");

		assertThat(secondTenantId).isEqualTo(firstTenantId);
		assertThat(countRows("tenants")).isEqualTo(1);
		assertThat(countRows("tenant_settings")).isEqualTo(5);
	}

	@Test
	void rejectsDisplayNameReuseForAnotherTenant() throws Exception {
		createTenant("Uwati Health Services Ltd.", "Uwati Health");

		mockMvc.perform(post("/api/platform/tenants")
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "legalName": "Another Organization",
						  "displayName": "Uwati Health"
						}
						"""))
				.andExpect(status().isConflict())
				.andExpect(status().reason("A tenant with the display name 'Uwati Health' already exists."));
	}

	private String createTenant(String legalName, String displayName) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/platform/tenants")
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "legalName": "%s",
						  "displayName": "%s"
						}
						""".formatted(legalName, displayName)))
				.andExpect(status().isCreated())
				.andReturn();
		return extractField(result.getResponse().getContentAsString(), "id");
	}

	private long countRows(String tableName) {
		return jdbcTemplate.queryForObject("select count(*) from " + tableName, Long.class);
	}

	private String extractField(String json, String fieldName) {
		Map<String, Object> fields = new JacksonJsonParser().parseMap(json);
		return fields.get(fieldName).toString();
	}
}
