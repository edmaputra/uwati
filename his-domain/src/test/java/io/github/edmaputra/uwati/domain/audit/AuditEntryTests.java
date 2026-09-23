package io.github.edmaputra.uwati.domain.audit;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * Unit tests for {@link AuditEntry}.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@DisplayName("AuditEntry Unit Tests")
class AuditEntryTests {

	@Test
	@DisplayName("compact constructor should validate required non-null fields")
	void constructor_withNullFields_throwsNullPointerException() {
		TenantId tenantId = TenantId.generate();
		Instant now = Instant.now();

		assertThatNullPointerException()
				.isThrownBy(() -> new AuditEntry(1L, tenantId, null, "1", "CREATE", "admin", "c-1", "{}", now))
				.withMessage("Entity name must not be null.");

		assertThatNullPointerException()
				.isThrownBy(() -> new AuditEntry(1L, tenantId, "Tenant", null, "CREATE", "admin", "c-1", "{}", now))
				.withMessage("Entity ID must not be null.");

		assertThatNullPointerException()
				.isThrownBy(() -> new AuditEntry(1L, tenantId, "Tenant", "1", null, "admin", "c-1", "{}", now))
				.withMessage("Action must not be null.");

		assertThatNullPointerException()
				.isThrownBy(() -> new AuditEntry(1L, tenantId, "Tenant", "1", "CREATE", null, "c-1", "{}", now))
				.withMessage("Actor must not be null.");

		assertThatNullPointerException()
				.isThrownBy(() -> new AuditEntry(1L, tenantId, "Tenant", "1", "CREATE", "admin", null, "{}", now))
				.withMessage("Correlation ID must not be null.");

		assertThatNullPointerException()
				.isThrownBy(() -> new AuditEntry(1L, tenantId, "Tenant", "1", "CREATE", "admin", "c-1", null, now))
				.withMessage("Changes JSON must not be null.");

		assertThatNullPointerException()
				.isThrownBy(() -> new AuditEntry(1L, tenantId, "Tenant", "1", "CREATE", "admin", "c-1", "{}", null))
				.withMessage("Occurred-at timestamp must not be null.");
	}

	@Test
	@DisplayName("static factory method of should construct valid AuditEntry")
	void of_withValidArguments_createsAuditEntry() {
		TenantId tenantId = TenantId.generate();
		AuditEntry entry = AuditEntry.of(tenantId, "Tenant", "t-123", "CREATE", "admin@hospital.org", "corr-1", "{}");

		assertThat(entry.id()).isNull();
		assertThat(entry.tenantId()).isEqualTo(tenantId);
		assertThat(entry.entityName()).isEqualTo("Tenant");
		assertThat(entry.entityId()).isEqualTo("t-123");
		assertThat(entry.action()).isEqualTo("CREATE");
		assertThat(entry.actor()).isEqualTo("admin@hospital.org");
		assertThat(entry.correlationId()).isEqualTo("corr-1");
		assertThat(entry.changesJson()).isEqualTo("{}");
		assertThat(entry.occurredAt()).isNotNull();
	}
}
