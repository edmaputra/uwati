package io.github.edmaputra.uwati.tenancy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.jayway.jsonpath.JsonPath;

import io.github.edmaputra.uwati.TestcontainersConfiguration;
import io.github.edmaputra.uwati.bootstrap.UwatiApplication;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = UwatiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Use Case: Configure Tenant Settings API")
class ConfigureTenantSettingsIntegrationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private JdbcTemplate jdbcTemplate;

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
	@DisplayName("Successful Configuration and Querying")
	class SuccessfulConfiguration {

		@Test
		@DisplayName("queries default settings and updates settings with incremented revisions and audit trail")
		void updatesSettingsAndIncrementsRevision() {
			String tenantId = createTenant("Uwati Health Services Ltd.", "Uwati Health");

			// 1. Query initial default settings
			byte[] getBytes = webTestClient.get()
					.uri("/api/platform/tenants/%s/settings".formatted(tenantId))
					.exchange()
					.expectStatus().isOk()
					.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
					.expectBody()
					.jsonPath("$.length()").isEqualTo(5)
					.returnResult()
					.getResponseBodyContent();

			assertThat(getBytes).isNotNull();
			String getJson = new String(getBytes, StandardCharsets.UTF_8);
			List<String> keys = JsonPath.read(getJson, "$[*].key");
			assertThat(keys).contains(
					"features.base-configuration",
					"finance.currency",
					"inventory.measurement-system",
					"organization.locale",
					"organization.time-zone");

			// 2. Configure / update settings with actor and correlation-id headers
			byte[] putBytes = webTestClient.put()
					.uri("/api/platform/tenants/%s/settings".formatted(tenantId))
					.header("X-Actor-Id", "operator-admin")
					.header("X-Correlation-Id", "corr-settings-update-999")
					.contentType(APPLICATION_JSON)
					.bodyValue("""
							{
							  "settings": [
							    {
							      "key": "organization.locale",
							      "value": "id-ID"
							    },
							    {
							      "key": "organization.time-zone",
							      "value": "Asia/Jakarta"
							    },
							    {
							      "key": "finance.currency",
							      "value": "IDR"
							    },
							    {
							      "key": "organization.contact-email",
							      "value": "admin@uwati.health"
							    }
							  ]
							}
							""")
					.exchange()
					.expectStatus().isOk()
					.expectHeader().valueEquals("X-Correlation-Id", "corr-settings-update-999")
					.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
					.expectBody()
					.jsonPath("$.length()").isEqualTo(4)
					.returnResult()
					.getResponseBodyContent();

			assertThat(putBytes).isNotNull();
			String putJson = new String(putBytes, StandardCharsets.UTF_8);

			// Verify updated revisions and values from response JSON
			List<Map<String, Object>> updatedSettings = JsonPath.read(putJson, "$");
			Map<String, Object> localeSetting = findSetting(updatedSettings, "organization.locale");
			assertThat(localeSetting.get("value")).isEqualTo("id-ID");
			assertThat(localeSetting.get("revision")).isEqualTo(2);

			Map<String, Object> tzSetting = findSetting(updatedSettings, "organization.time-zone");
			assertThat(tzSetting.get("value")).isEqualTo("Asia/Jakarta");
			assertThat(tzSetting.get("revision")).isEqualTo(2);

			Map<String, Object> currencySetting = findSetting(updatedSettings, "finance.currency");
			assertThat(currencySetting.get("value")).isEqualTo("IDR");
			assertThat(currencySetting.get("revision")).isEqualTo(2);

			Map<String, Object> emailSetting = findSetting(updatedSettings, "organization.contact-email");
			assertThat(emailSetting.get("value")).isEqualTo("admin@uwati.health");
			assertThat(emailSetting.get("revision")).isEqualTo(1);

			// 3. Verify PostgreSQL database state
			UUID tenantUuid = UUID.fromString(tenantId);
			Map<String, Object> localeRow = jdbcTemplate.queryForMap(
					"select setting_value, revision from tenant_settings where tenant_id = ? and setting_key = 'organization.locale'",
					tenantUuid);
			assertThat(localeRow.get("setting_value")).isEqualTo("id-ID");
			assertThat(localeRow.get("revision")).isEqualTo(2);

			Map<String, Object> emailRow = jdbcTemplate.queryForMap(
					"select setting_value, revision from tenant_settings where tenant_id = ? and setting_key = 'organization.contact-email'",
					tenantUuid);
			assertThat(emailRow.get("setting_value")).isEqualTo("admin@uwati.health");
			assertThat(emailRow.get("revision")).isEqualTo(1);

			// 4. Verify audit trail entries in database
			List<Map<String, Object>> auditRows = jdbcTemplate.queryForList(
					"select tenant_id, entity_name, entity_id, action, actor, correlation_id, changes_json from audit_entries where tenant_id = ? order by id",
					tenantUuid);
			assertThat(auditRows).hasSize(2);

			// Audit 1: Tenant Creation
			Map<String, Object> tenantAudit = auditRows.get(0);
			assertThat(tenantAudit.get("entity_name")).isEqualTo("Tenant");
			assertThat(tenantAudit.get("entity_id")).isEqualTo(tenantId);
			assertThat(tenantAudit.get("action")).isEqualTo("CREATE");
			String tenantChangesJson = (String) tenantAudit.get("changes_json");
			assertThat(tenantChangesJson).doesNotContain("\"fields\":");
			assertThat(tenantChangesJson).contains("\"displayName\":{\"old\":null,\"new\":\"Uwati Health\"}");
			assertThat(tenantChangesJson).contains("\"legalName\":{\"old\":null,\"new\":\"Uwati Health Services Ltd.\"}");
			assertThat(tenantChangesJson).contains("\"status\":{\"old\":null,\"new\":\"ACTIVE\"}");

			// Audit 2: Tenant Settings Configuration
			Map<String, Object> settingsAudit = auditRows.get(1);
			assertThat(settingsAudit.get("entity_name")).isEqualTo("TenantSetting");
			assertThat(settingsAudit.get("entity_id")).isEqualTo(tenantId);
			assertThat(settingsAudit.get("action")).isEqualTo("UPDATE");
			assertThat(settingsAudit.get("actor")).isEqualTo("operator-admin");
			assertThat(settingsAudit.get("correlation_id")).isEqualTo("corr-settings-update-999");

			String settingsChangesJson = (String) settingsAudit.get("changes_json");

			// Assert settings collection diff structure without "collections" wrapper
			assertThat(settingsChangesJson).doesNotContain("\"collections\":");
			assertThat(settingsChangesJson).contains("\"settings\":{");

			// Assert Added elements in String JSON
			assertThat(settingsChangesJson).contains("\"added\":[{\"key\":\"organization.contact-email\",\"value\":\"admin@uwati.health\",\"revision\":1}]");

			// Assert Removed elements in String JSON
			assertThat(settingsChangesJson).contains("\"removed\":[]");

			// Assert Changed elements in String JSON (with deterministic alphabetical field ordering: revision then value, without nested "fields")
			assertThat(settingsChangesJson).contains("{\"key\":\"organization.locale\",\"revision\":{\"old\":1,\"new\":2},\"value\":{\"old\":\"en-US\",\"new\":\"id-ID\"}}");
			assertThat(settingsChangesJson).contains("{\"key\":\"organization.time-zone\",\"revision\":{\"old\":1,\"new\":2},\"value\":{\"old\":\"UTC\",\"new\":\"Asia/Jakarta\"}}");
			assertThat(settingsChangesJson).contains("{\"key\":\"finance.currency\",\"revision\":{\"old\":1,\"new\":2},\"value\":{\"old\":\"USD\",\"new\":\"IDR\"}}");

			// JsonPath assertions on the JSON string
			List<String> addedKeys = JsonPath.read(settingsChangesJson, "$.settings.added[*].key");
			assertThat(addedKeys).containsExactly("organization.contact-email");

			List<String> changedKeys = JsonPath.read(settingsChangesJson, "$.settings.changed[*].key");
			assertThat(changedKeys).containsExactlyInAnyOrder("organization.locale", "organization.time-zone", "finance.currency");

			String localeOldVal = JsonPath.read(settingsChangesJson, "$.settings.changed[?(@.key=='organization.locale')].value.old.get(0)");
			String localeNewVal = JsonPath.read(settingsChangesJson, "$.settings.changed[?(@.key=='organization.locale')].value.new.get(0)");
			assertThat(localeOldVal).isEqualTo("en-US");
			assertThat(localeNewVal).isEqualTo("id-ID");
		}
	}

	@Nested
	@DisplayName("Validation Failures")
	class ValidationFailures {

		@Test
		@DisplayName("rejects configuration when setting key is unknown / unsupported")
		void rejectsUnsupportedSettingKey() {
			String tenantId = createTenant("Uwati Health Services Ltd.", "Uwati Health");

			webTestClient.put()
					.uri("/api/platform/tenants/%s/settings".formatted(tenantId))
					.contentType(APPLICATION_JSON)
					.bodyValue("""
							{
							  "settings": [
							    {
							      "key": "unsupported.custom-key",
							      "value": "custom-val"
							    }
							  ]
							}
							""")
					.exchange()
					.expectStatus().isBadRequest();
		}

		@Test
		@DisplayName("rejects configuration when timezone is invalid")
		void rejectsInvalidTimeZone() {
			String tenantId = createTenant("Uwati Health Services Ltd.", "Uwati Health");

			webTestClient.put()
					.uri("/api/platform/tenants/%s/settings".formatted(tenantId))
					.contentType(APPLICATION_JSON)
					.bodyValue("""
							{
							  "settings": [
							    {
							      "key": "organization.time-zone",
							      "value": "Invalid/Timezone"
							    }
							  ]
							}
							""")
					.exchange()
					.expectStatus().isBadRequest();
		}

		@Test
		@DisplayName("rejects configuration when currency code is invalid")
		void rejectsInvalidCurrencyCode() {
			String tenantId = createTenant("Uwati Health Services Ltd.", "Uwati Health");

			webTestClient.put()
					.uri("/api/platform/tenants/%s/settings".formatted(tenantId))
					.contentType(APPLICATION_JSON)
					.bodyValue("""
							{
							  "settings": [
							    {
							      "key": "finance.currency",
							      "value": "XYZ_INVALID"
							    }
							  ]
							}
							""")
					.exchange()
					.expectStatus().isBadRequest();
		}

		@Test
		@DisplayName("rejects configuration when measurement system is invalid")
		void rejectsInvalidMeasurementSystem() {
			String tenantId = createTenant("Uwati Health Services Ltd.", "Uwati Health");

			webTestClient.put()
					.uri("/api/platform/tenants/%s/settings".formatted(tenantId))
					.contentType(APPLICATION_JSON)
					.bodyValue("""
							{
							  "settings": [
							    {
							      "key": "inventory.measurement-system",
							      "value": "UNKNOWN_SYSTEM"
							    }
							  ]
							}
							""")
					.exchange()
					.expectStatus().isBadRequest();
		}

		@Test
		@DisplayName("rejects configuration when email address format is invalid")
		void rejectsInvalidEmail() {
			String tenantId = createTenant("Uwati Health Services Ltd.", "Uwati Health");

			webTestClient.put()
					.uri("/api/platform/tenants/%s/settings".formatted(tenantId))
					.contentType(APPLICATION_JSON)
					.bodyValue("""
							{
							  "settings": [
							    {
							      "key": "organization.contact-email",
							      "value": "not-a-valid-email"
							    }
							  ]
							}
							""")
					.exchange()
					.expectStatus().isBadRequest();
		}

		@Test
		@DisplayName("rejects configuration when settings list is empty")
		void rejectsEmptySettingsList() {
			String tenantId = createTenant("Uwati Health Services Ltd.", "Uwati Health");

			webTestClient.put()
					.uri("/api/platform/tenants/%s/settings".formatted(tenantId))
					.contentType(APPLICATION_JSON)
					.bodyValue("""
							{
							  "settings": []
							}
							""")
					.exchange()
					.expectStatus().isBadRequest();
		}
	}

	@Nested
	@DisplayName("Tenant Not Found Failures")
	class TenantNotFoundFailures {

		@Test
		@DisplayName("returns 404 Not Found when querying settings for non-existent tenant")
		void returns404OnGetNonExistentTenant() {
			UUID nonExistent = UUID.randomUUID();

			webTestClient.get()
					.uri("/api/platform/tenants/%s/settings".formatted(nonExistent))
					.exchange()
					.expectStatus().isNotFound();
		}

		@Test
		@DisplayName("returns 404 Not Found when configuring settings for non-existent tenant")
		void returns404OnPutNonExistentTenant() {
			UUID nonExistent = UUID.randomUUID();

			webTestClient.put()
					.uri("/api/platform/tenants/%s/settings".formatted(nonExistent))
					.contentType(APPLICATION_JSON)
					.bodyValue("""
							{
							  "settings": [
							    {
							      "key": "organization.locale",
							      "value": "en-US"
							    }
							  ]
							}
							""")
					.exchange()
					.expectStatus().isNotFound();
		}
	}

	private String createTenant(String legalName, String displayName) {
		byte[] responseBytes = webTestClient.post()
				.uri("/api/platform/tenants")
				.header("X-Actor-Id", "operator-creator")
				.header("X-Correlation-Id", "corr-create-tenant-001")
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

	private Map<String, Object> findSetting(List<Map<String, Object>> list, String key) {
		return list.stream()
				.filter(s -> key.equals(s.get("key")))
				.findFirst()
				.orElseThrow(() -> new AssertionError("Setting with key '%s' not found".formatted(key)));
	}
}
