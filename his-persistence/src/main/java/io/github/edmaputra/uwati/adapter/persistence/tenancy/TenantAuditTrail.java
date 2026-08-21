package io.github.edmaputra.uwati.adapter.persistence.tenancy;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantCreated;
import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantSettingsUpdated;
import lombok.RequiredArgsConstructor;

/**
 * Records immutable audit-trail entries for tenant lifecycle and configuration events.
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

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void onTenantSettingsUpdated(TenantSettingsUpdated event) {
		String details = event.updatedSettings().stream()
				.map(s -> "{\"key\":\"%s\",\"value\":\"%s\",\"revision\":%d}".formatted(
						escapeJson(s.key()),
						escapeJson(s.value()),
						s.revision()))
				.collect(Collectors.joining(",", "{\"updatedSettings\":[", "]}"));

		tenantAuditEntries.save(new TenantAuditEntryEntity(
				event.tenantId().value(),
				"TENANT_SETTINGS_CONFIGURED",
				event.occurredAt(),
				details));
	}

	private String escapeJson(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
