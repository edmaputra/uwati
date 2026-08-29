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

import tools.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import io.github.edmaputra.uwati.TestcontainersConfiguration;
import io.github.edmaputra.uwati.bootstrap.UwatiApplication;
import io.github.edmaputra.uwati.test.RequiresDocker;

@RequiresDocker
@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = UwatiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Use Case: Configure Tenant Settings API")
class ConfigureTenantSettingsIntegrationTests {

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
	@DisplayName("Successful Configuration and Querying")
	class SuccessfulConfiguration {

		@Test
		@DisplayName("queries default settings and updates settings with incremented revisions and audit trail")
		void updatesSettingsAndIncrementsRevision() throws Exception {
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
			webTestClient.put()
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
					.json("""
							[
							  {
							    "key": "organization.locale",
							    "value": "id-ID",
							    "revision": 2
							  },
							  {
							    "key": "organization.time-zone",
							    "value": "Asia/Jakarta",
							    "revision": 2
							  },
							  {
							    "key": "finance.currency",
							    "value": "IDR",
							    "revision": 2
							  },
							  {
							    "key": "organization.contact-email",
							    "value": "admin@uwati.health",
							    "revision": 1
							  }
							]
							""");

			// 3. Verify PostgreSQL database state
			UUID tenantUuid = UUID.fromString(tenantId);
			Map<String, Object> localeRow = jdbcTemplate.queryForMap(
					"select setting_value, revision from tenant_settings where tenant_id = ? and setting_key = 'organization.locale'",
					tenantUuid);
			JSONAssert.assertEquals("""
					{
					  "setting_value": "id-ID",
					  "revision": 2
					}
					""", objectMapper.writeValueAsString(localeRow), JSONCompareMode.LENIENT);

			Map<String, Object> emailRow = jdbcTemplate.queryForMap(
					"select setting_value, revision from tenant_settings where tenant_id = ? and setting_key = 'organization.contact-email'",
					tenantUuid);
			JSONAssert.assertEquals("""
					{
					  "setting_value": "admin@uwati.health",
					  "revision": 1
					}
					""", objectMapper.writeValueAsString(emailRow), JSONCompareMode.LENIENT);

			// 4. Verify audit trail entries in database
			List<Map<String, Object>> auditRows = jdbcTemplate.queryForList(
					"select tenant_id, entity_name, entity_id, action, actor, correlation_id, changes_json from audit_entries where tenant_id = ? order by id",
					tenantUuid);
			assertThat(auditRows).hasSize(2);

			// Audit 1: Tenant Creation
			Map<String, Object> tenantAudit = auditRows.get(0);
			JSONAssert.assertEquals("""
					{
					  "tenant_id": "%s",
					  "entity_name": "Tenant",
					  "entity_id": "%s",
					  "action": "CREATE",
					  "actor": "operator-creator",
					  "correlation_id": "corr-create-tenant-001",
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
					""".formatted(tenantUuid, tenantId), toAuditJson(tenantAudit), JSONCompareMode.LENIENT);

			// Audit 2: Tenant Settings Configuration
			Map<String, Object> settingsAudit = auditRows.get(1);
			JSONAssert.assertEquals("""
					{
					  "tenant_id": "%s",
					  "entity_name": "TenantSetting",
					  "entity_id": "%s",
					  "action": "UPDATE",
					  "actor": "operator-admin",
					  "correlation_id": "corr-settings-update-999",
					  "changes": {
					    "settings": {
					      "added": [
					        {
					          "key": "organization.contact-email",
					          "value": "admin@uwati.health",
					          "revision": 1
					        }
					      ],
					      "removed": [],
					      "changed": [
					        {
					          "key": "organization.locale",
					          "revision": {
					            "old": 1,
					            "new": 2
					          },
					          "value": {
					            "old": "en-US",
					            "new": "id-ID"
					          }
					        },
					        {
					          "key": "organization.time-zone",
					          "revision": {
					            "old": 1,
					            "new": 2
					          },
					          "value": {
					            "old": "UTC",
					            "new": "Asia/Jakarta"
					          }
					        },
					        {
					          "key": "finance.currency",
					          "revision": {
					            "old": 1,
					            "new": 2
					          },
					          "value": {
					            "old": "USD",
					            "new": "IDR"
					          }
					        }
					      ]
					    }
					  }
					}
					""".formatted(tenantUuid, tenantId), toAuditJson(settingsAudit), JSONCompareMode.LENIENT);
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
							      "key": "unknown.custom-setting",
							      "value": "some-value"
							    }
							  ]
							}
							""")
					.exchange()
					.expectStatus().isBadRequest();
		}

		@Test
		@DisplayName("rejects configuration when currency code is invalid")
		void rejectsInvalidCurrency() {
			String tenantId = createTenant("Uwati Health Services Ltd.", "Uwati Health");

			webTestClient.put()
					.uri("/api/platform/tenants/%s/settings".formatted(tenantId))
					.contentType(APPLICATION_JSON)
					.bodyValue("""
							{
							  "settings": [
							    {
							      "key": "finance.currency",
							      "value": "INVALID_CURRENCY"
							    }
							  ]
							}
							""")
					.exchange()
					.expectStatus().isBadRequest();
		}

		@Test
		@DisplayName("rejects configuration when locale tag is invalid")
		void rejectsInvalidLocale() {
			String tenantId = createTenant("Uwati Health Services Ltd.", "Uwati Health");

			webTestClient.put()
					.uri("/api/platform/tenants/%s/settings".formatted(tenantId))
					.contentType(APPLICATION_JSON)
					.bodyValue("""
							{
							  "settings": [
							    {
							      "key": "organization.locale",
							      "value": "invalid-locale-tag"
							    }
							  ]
							}
							""")
					.exchange()
					.expectStatus().isBadRequest();
		}

		@Test
		@DisplayName("rejects configuration when time zone identifier is invalid")
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

	private Map<String, Object> findSetting(List<Map<String, Object>> list, String key) {
		return list.stream()
				.filter(s -> key.equals(s.get("key")))
				.findFirst()
				.orElseThrow(() -> new AssertionError("Setting with key '%s' not found".formatted(key)));
	}
}
