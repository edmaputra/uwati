package io.github.edmaputra.uwati.tenancy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import io.github.edmaputra.uwati.TestcontainersConfiguration;
import io.github.edmaputra.uwati.bootstrap.UwatiApplication;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = UwatiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Use Case: Create Tenant API")
class CreateTenantIntegrationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	private WebTestClient webTestClient;

	@BeforeEach
	void setup() {
		webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
		jdbcTemplate.update("delete from audit_entries");
		jdbcTemplate.update("delete from tenant_document_sequences");
		jdbcTemplate.update("delete from tenant_settings");
		jdbcTemplate.update("delete from tenants");
	}

	@Nested
	@DisplayName("Successful Tenant Creation")
	class SuccessfulCreation {

		@Test
		@DisplayName("creates an active tenant, publishes TenantCreated event, and provisions default settings, sequences, and audit log")
		void createsActiveTenantAndProvisionsDefaults() throws Exception {
			byte[] responseBytes = webTestClient.post()
					.uri("/api/platform/tenants")
					.header("X-Actor-Id", "operator-creator")
					.header("X-Correlation-Id", "corr-create-tenant-123")
					.contentType(APPLICATION_JSON)
					.bodyValue("""
							{
							  "legalName": "Uwati Health Services Ltd.",
							  "displayName": "Uwati Health"
							}
							""")
					.exchange()
					.expectStatus().isCreated()
					.expectHeader().valueEquals("X-Correlation-Id", "corr-create-tenant-123")
					.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
					.expectBody()
					.json("""
							{
							  "legalName": "Uwati Health Services Ltd.",
							  "displayName": "Uwati Health",
							  "status": "ACTIVE"
							}
							""")
					.jsonPath("$.id").isNotEmpty()
					.jsonPath("$.createdAt").isNotEmpty()
					.jsonPath("$.updatedAt").isNotEmpty()
					.returnResult()
					.getResponseBodyContent();

			assertThat(responseBytes).isNotNull();
			String jsonString = new String(responseBytes, StandardCharsets.UTF_8);
			String tenantIdStr = JsonPath.read(jsonString, "$.id");
			UUID tenantId = UUID.fromString(tenantIdStr);

			// Verify tenant persisted in database
			assertThat(countRowsWhere("tenants", "id = '" + tenantId + "'")).isEqualTo(1);
			Map<String, Object> tenantRow = jdbcTemplate.queryForMap(
					"select legal_name, display_name, status from tenants where id = ?", tenantId);
			JSONAssert.assertEquals("""
					{
					  "legal_name": "Uwati Health Services Ltd.",
					  "display_name": "Uwati Health",
					  "status": "ACTIVE"
					}
					""", objectMapper.writeValueAsString(tenantRow), JSONCompareMode.LENIENT);

			// Verify default tenant settings provisioned
			List<String> settingKeys = jdbcTemplate.queryForList(
					"select setting_key from tenant_settings where tenant_id = ? order by setting_key",
					String.class,
					tenantId);
			assertThat(settingKeys).containsExactly(
					"features.base-configuration",
					"finance.currency",
					"inventory.measurement-system",
					"organization.locale",
					"organization.time-zone");

			// Verify default document sequences provisioned
			List<String> documentTypes = jdbcTemplate.queryForList(
					"select document_type from tenant_document_sequences where tenant_id = ? order by document_type",
					String.class,
					tenantId);
			assertThat(documentTypes).containsExactly(
					"ENCOUNTER",
					"INVOICE",
					"PATIENT",
					"PRESCRIPTION",
					"PURCHASE");

			// Verify audit trail entry recorded in database
			List<Map<String, Object>> auditEntries = jdbcTemplate.queryForList(
					"select tenant_id, entity_name, entity_id, action, actor, correlation_id, changes_json from audit_entries where tenant_id = ?",
					tenantId);
			assertThat(auditEntries).hasSize(1);
			Map<String, Object> audit = auditEntries.get(0);

			JSONAssert.assertEquals("""
					{
					  "tenant_id": "%s",
					  "entity_name": "Tenant",
					  "entity_id": "%s",
					  "action": "CREATE",
					  "actor": "operator-creator",
					  "correlation_id": "corr-create-tenant-123",
					  "changes": {
					    "displayName": {
					      "old": null,
					      "new": "Uwati Health"
					    },
					    "legalName": {
					      "old": null,
					      "new": "Uwati Health Services Ltd."
					    },
					    "status": {
					      "old": null,
					      "new": "ACTIVE"
					    }
					  }
					}
					""".formatted(tenantId, tenantIdStr), toAuditJson(audit), JSONCompareMode.LENIENT);
		}

		@Test
		@DisplayName("returns existing tenant when request is re-submitted with identical legal and display name (idempotency)")
		void returnsExistingTenantOnIdempotentRetry() {
			String firstTenantId = createTenant("Uwati Health Services Ltd.", "Uwati Health");
			String secondTenantId = createTenant("Uwati Health Services Ltd.", "Uwati Health");

			assertThat(secondTenantId).isEqualTo(firstTenantId);
			assertThat(countRows("tenants")).isEqualTo(1);
			assertThat(countRows("tenant_settings")).isEqualTo(5);
			assertThat(countRows("tenant_document_sequences")).isEqualTo(5);
			assertThat(countRows("audit_entries")).isEqualTo(1);
		}
	}

	@Nested
	@DisplayName("Conflict and Validation Failures")
	class ValidationAndConflictFailures {

		@Test
		@DisplayName("rejects creation with 409 Conflict when display name is already used by another legal entity")
		void rejectsDuplicateDisplayNameForDifferentLegalName() {
			createTenant("Uwati Health Services Ltd.", "Uwati Health");

			webTestClient.post()
					.uri("/api/platform/tenants")
					.contentType(APPLICATION_JSON)
					.bodyValue("""
							{
							  "legalName": "Another Medical Group",
							  "displayName": "Uwati Health"
							}
							""")
					.exchange()
					.expectStatus().isEqualTo(409);

			assertThat(countRows("tenants")).isEqualTo(1);
		}

		@Test
		@DisplayName("rejects creation with 400 Bad Request when legal name is blank")
		void rejectsBlankLegalName() {
			webTestClient.post()
					.uri("/api/platform/tenants")
					.contentType(APPLICATION_JSON)
					.bodyValue("""
							{
							  "legalName": "   ",
							  "displayName": "Uwati Health"
							}
							""")
					.exchange()
					.expectStatus().isBadRequest();

			assertThat(countRows("tenants")).isZero();
		}

		@Test
		@DisplayName("rejects creation with 400 Bad Request when display name is blank")
		void rejectsBlankDisplayName() {
			webTestClient.post()
					.uri("/api/platform/tenants")
					.contentType(APPLICATION_JSON)
					.bodyValue("""
							{
							  "legalName": "Uwati Health Services Ltd.",
							  "displayName": "   "
							}
							""")
					.exchange()
					.expectStatus().isBadRequest();

			assertThat(countRows("tenants")).isZero();
		}
	}

	private String createTenant(String legalName, String displayName) {
		byte[] responseBytes = webTestClient.post()
				.uri("/api/platform/tenants")
				.header("X-Actor-Id", "operator-creator")
				.header("X-Correlation-Id", "corr-create-tenant-idempotent")
				.contentType(APPLICATION_JSON)
				.bodyValue("""
						{
						  "legalName": "%s",
						  "displayName": "%s"
						}
						""".formatted(legalName, displayName))
				.exchange()
				.expectStatus().isCreated()
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.returnResult()
				.getResponseBodyContent();

		assertThat(responseBytes).isNotNull();
		String jsonString = new String(responseBytes, StandardCharsets.UTF_8);
		return JsonPath.read(jsonString, "$.id");
	}

	private String toAuditJson(Map<String, Object> row) {
		Map<String, Object> map = new LinkedHashMap<>(row);
		if (map.containsKey("changes_json")) {
			try {
				map.put("changes", objectMapper.readTree((String) map.remove("changes_json")));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		try {
			return objectMapper.writeValueAsString(map);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private long countRows(String tableName) {
		return jdbcTemplate.queryForObject("select count(*) from " + tableName, Long.class);
	}

	private long countRowsWhere(String tableName, String condition) {
		return jdbcTemplate.queryForObject("select count(*) from " + tableName + " where " + condition, Long.class);
	}
}
