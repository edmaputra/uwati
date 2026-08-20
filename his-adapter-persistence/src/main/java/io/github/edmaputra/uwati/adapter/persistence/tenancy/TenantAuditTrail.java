package io.github.edmaputra.uwati.adapter.persistence.tenancy;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantCreated;
import lombok.RequiredArgsConstructor;

/**
 * Records an immutable audit-trail entry when a tenant is created.
 */
@Component
@RequiredArgsConstructor
public class TenantAuditTrail {

	private final TenantAuditEntryJpaRepository tenantAuditEntries;

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void onTenantCreated(TenantCreated event) {
		var tenant = event.tenant();
		tenantAuditEntries.save(new TenantAuditEntryEntity(
				tenant.id().value(),
				"TENANT_CREATED",
				event.occurredAt(),
				"{\"displayName\":\"%s\",\"legalName\":\"%s\"}".formatted(
						escapeJson(tenant.displayName()),
						escapeJson(tenant.legalName()))));
	}

	private String escapeJson(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
