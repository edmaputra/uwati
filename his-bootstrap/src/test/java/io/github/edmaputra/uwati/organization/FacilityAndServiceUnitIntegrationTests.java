package io.github.edmaputra.uwati.organization;

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
import io.github.edmaputra.uwati.test.RequiresDocker;

@RequiresDocker
@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = UwatiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Use Case: Facilities & Service Units Management API")
class FacilityAndServiceUnitIntegrationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private WebTestClient webTestClient;

	private final String tenantIdA = "01918a20-0000-7000-8000-000000000001";
	private final String tenantIdB = "01918a20-0000-7000-8000-000000000002";

	@BeforeEach
	void setup() {
		webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
		jdbcTemplate.update("delete from audit_entries");
		jdbcTemplate.update("delete from service_units");
		jdbcTemplate.update("delete from facilities");
		jdbcTemplate.update("delete from tenant_settings");
		jdbcTemplate.update("delete from tenant_document_sequences");
		jdbcTemplate.update("delete from tenants");

		// Seed tenants in database
		seedTenant(tenantIdA, "Hospital Group A", "RS A");
		seedTenant(tenantIdB, "Hospital Group B", "RS B");
	}

	private void seedTenant(String id, String legalName, String displayName) {
		jdbcTemplate.update("""
				insert into tenants (id, legal_name, display_name, display_name_normalized, status, created_at, updated_at)
				values (?, ?, ?, ?, 'ACTIVE', now(), now())
				""",
				UUID.fromString(id), legalName, displayName, displayName.toLowerCase());
	}

	@Nested
	@DisplayName("Facility Endpoints")
	class FacilityEndpoints {

		@Test
		@DisplayName("creates a facility, publishes event, persists audit entry, and allows retrieval")
		void createsAndRetrievesFacility() {
			byte[] createResponse = webTestClient.post()
					.uri("/api/v1/facilities")
					.header("X-Tenant-Id", tenantIdA)
					.header("X-Actor-Id", "admin-1")
					.header("X-Correlation-Id", "corr-fac-01")
					.contentType(APPLICATION_JSON)
					.bodyValue("""
							{
							  "code": "RS-PUSAT",
							  "name": "RS Uwati Pusat",
							  "type": "HOSPITAL",
							  "classification": "CLASS_B",
							  "nationalRegistryCode": "3171012",
							  "address": "Jl. Gatot Subroto No. 10",
							  "phone": "021-1234567"
							}
							""")
					.exchange()
					.expectStatus().isCreated()
					.expectHeader().valueEquals("X-Correlation-Id", "corr-fac-01")
					.expectBody()
					.jsonPath("$.id").isNotEmpty()
					.jsonPath("$.tenantId").isEqualTo(tenantIdA)
					.jsonPath("$.code").isEqualTo("RS-PUSAT")
					.jsonPath("$.name").isEqualTo("RS Uwati Pusat")
					.jsonPath("$.type").isEqualTo("HOSPITAL")
					.jsonPath("$.classification").isEqualTo("CLASS_B")
					.jsonPath("$.status").isEqualTo("ACTIVE")
					.returnResult()
					.getResponseBodyContent();

			assertThat(createResponse).isNotNull();
			String facilityId = JsonPath.read(new String(createResponse, StandardCharsets.UTF_8), "$.id");

			// Verify audit entry in database
			List<Map<String, Object>> auditLogs = jdbcTemplate.queryForList(
					"select * from audit_entries where entity_name = 'Facility' and entity_id = ?",
					facilityId);
			assertThat(auditLogs).hasSize(1);
			assertThat(auditLogs.getFirst().get("action")).isEqualTo("CREATE");
			assertThat(auditLogs.getFirst().get("actor")).isEqualTo("admin-1");

			// Retrieve by ID
			webTestClient.get()
					.uri("/api/v1/facilities/" + facilityId)
					.header("X-Tenant-Id", tenantIdA)
					.exchange()
					.expectStatus().isOk()
					.expectBody()
					.jsonPath("$.id").isEqualTo(facilityId)
					.jsonPath("$.code").isEqualTo("RS-PUSAT");

			// Update facility
			webTestClient.put()
					.uri("/api/v1/facilities/" + facilityId)
					.header("X-Tenant-Id", tenantIdA)
					.header("X-Actor-Id", "admin-updater")
					.header("X-Correlation-Id", "corr-fac-upd")
					.contentType(APPLICATION_JSON)
					.bodyValue("""
							{
							  "name": "RS Uwati Pusat Modern",
							  "type": "HOSPITAL",
							  "classification": "CLASS_A",
							  "nationalRegistryCode": "3171012-A",
							  "address": "Jl. Gatot Subroto No. 12",
							  "phone": "021-7654321"
							}
							""")
					.exchange()
					.expectStatus().isOk()
					.expectBody()
					.jsonPath("$.name").isEqualTo("RS Uwati Pusat Modern")
					.jsonPath("$.classification").isEqualTo("CLASS_A");

			// Verify audit entry for update
			auditLogs = jdbcTemplate.queryForList(
					"select * from audit_entries where entity_name = 'Facility' and entity_id = ? order by occurred_at desc",
					facilityId);
			assertThat(auditLogs).hasSize(2);
			assertThat(auditLogs.getFirst().get("action")).isEqualTo("UPDATE");

			// Change status
			webTestClient.patch()
					.uri("/api/v1/facilities/" + facilityId + "/status")
					.header("X-Tenant-Id", tenantIdA)
					.header("X-Actor-Id", "admin-deactivator")
					.contentType(APPLICATION_JSON)
					.bodyValue("""
							{
							  "status": "INACTIVE"
							}
							""")
					.exchange()
					.expectStatus().isOk()
					.expectBody()
					.jsonPath("$.status").isEqualTo("INACTIVE");

			// Verify duplicate code rejected
			webTestClient.post()
					.uri("/api/v1/facilities")
					.header("X-Tenant-Id", tenantIdA)
					.contentType(APPLICATION_JSON)
					.bodyValue("""
							{
							  "code": "RS-PUSAT",
							  "name": "Another Hospital Same Code",
							  "type": "HOSPITAL",
							  "classification": "CLASS_C"
							}
							""")
					.exchange()
					.expectStatus().isEqualTo(409);

			// Multi-tenant isolation: Tenant B cannot see Tenant A's facility
			webTestClient.get()
					.uri("/api/v1/facilities/" + facilityId)
					.header("X-Tenant-Id", tenantIdB)
					.exchange()
					.expectStatus().isNotFound();
		}
	}

	@Nested
	@DisplayName("Service Unit Endpoints")
	class ServiceUnitEndpoints {

		@Test
		@DisplayName("creates a service unit under a facility, allows search, update, and status change")
		void createsAndManagesServiceUnit() {
			// First create a facility under Tenant A
			byte[] facResponse = webTestClient.post()
					.uri("/api/v1/facilities")
					.header("X-Tenant-Id", tenantIdA)
					.contentType(APPLICATION_JSON)
					.bodyValue("""
							{
							  "code": "FAC-TEST",
							  "name": "Klinik Utama Uwati",
							  "type": "CLINIC",
							  "classification": "UTAMA"
							}
							""")
					.exchange()
					.expectStatus().isCreated()
					.returnResult()
					.getResponseBodyContent();

			assertThat(facResponse).isNotNull();
			String facilityId = JsonPath.read(new String(facResponse, StandardCharsets.UTF_8), "$.id");

			// Create a service unit
			byte[] unitResponse = webTestClient.post()
					.uri("/api/v1/service-units")
					.header("X-Tenant-Id", tenantIdA)
					.header("X-Actor-Id", "unit-creator")
					.header("X-Correlation-Id", "corr-unit-01")
					.contentType(APPLICATION_JSON)
					.bodyValue(String.format("""
							{
							  "facilityId": "%s",
							  "code": "POLI-UMUM",
							  "name": "Poli Umum",
							  "type": "OUTPATIENT_CLINIC"
							}
							""", facilityId))
					.exchange()
					.expectStatus().isCreated()
					.expectHeader().valueEquals("X-Correlation-Id", "corr-unit-01")
					.expectBody()
					.jsonPath("$.id").isNotEmpty()
					.jsonPath("$.tenantId").isEqualTo(tenantIdA)
					.jsonPath("$.facilityId").isEqualTo(facilityId)
					.jsonPath("$.code").isEqualTo("POLI-UMUM")
					.jsonPath("$.name").isEqualTo("Poli Umum")
					.jsonPath("$.type").isEqualTo("OUTPATIENT_CLINIC")
					.jsonPath("$.status").isEqualTo("ACTIVE")
					.returnResult()
					.getResponseBodyContent();

			assertThat(unitResponse).isNotNull();
			String serviceUnitId = JsonPath.read(new String(unitResponse, StandardCharsets.UTF_8), "$.id");

			// Verify audit entry for service unit
			List<Map<String, Object>> auditLogs = jdbcTemplate.queryForList(
					"select * from audit_entries where entity_name = 'ServiceUnit' and entity_id = ?",
					serviceUnitId);
			assertThat(auditLogs).hasSize(1);
			assertThat(auditLogs.getFirst().get("action")).isEqualTo("CREATE");

			// Get by ID
			webTestClient.get()
					.uri("/api/v1/service-units/" + serviceUnitId)
					.header("X-Tenant-Id", tenantIdA)
					.exchange()
					.expectStatus().isOk()
					.expectBody()
					.jsonPath("$.id").isEqualTo(serviceUnitId)
					.jsonPath("$.name").isEqualTo("Poli Umum");

			// List by facilityId
			webTestClient.get()
					.uri("/api/v1/service-units?facilityId=" + facilityId)
					.header("X-Tenant-Id", tenantIdA)
					.exchange()
					.expectStatus().isOk()
					.expectBody()
					.jsonPath("$.length()").isEqualTo(1)
					.jsonPath("$[0].code").isEqualTo("POLI-UMUM");

			// Update service unit
			webTestClient.put()
					.uri("/api/v1/service-units/" + serviceUnitId)
					.header("X-Tenant-Id", tenantIdA)
					.contentType(APPLICATION_JSON)
					.bodyValue("""
							{
							  "name": "Poli Umum & Lansia",
							  "type": "OUTPATIENT_CLINIC"
							}
							""")
					.exchange()
					.expectStatus().isOk()
					.expectBody()
					.jsonPath("$.name").isEqualTo("Poli Umum & Lansia");

			// Change status
			webTestClient.patch()
					.uri("/api/v1/service-units/" + serviceUnitId + "/status")
					.header("X-Tenant-Id", tenantIdA)
					.contentType(APPLICATION_JSON)
					.bodyValue("""
							{
							  "status": "INACTIVE"
							}
							""")
					.exchange()
					.expectStatus().isOk()
					.expectBody()
					.jsonPath("$.status").isEqualTo("INACTIVE");

			// Reject duplicate unit code in same facility
			webTestClient.post()
					.uri("/api/v1/service-units")
					.header("X-Tenant-Id", tenantIdA)
					.contentType(APPLICATION_JSON)
					.bodyValue(String.format("""
							{
							  "facilityId": "%s",
							  "code": "POLI-UMUM",
							  "name": "Duplicate Code",
							  "type": "OUTPATIENT_CLINIC"
							}
							""", facilityId))
					.exchange()
					.expectStatus().isEqualTo(409);

			// Multi-tenant isolation: Tenant B cannot access Tenant A's service unit
			webTestClient.get()
					.uri("/api/v1/service-units/" + serviceUnitId)
					.header("X-Tenant-Id", tenantIdB)
					.exchange()
					.expectStatus().isNotFound();
		}
	}
}
