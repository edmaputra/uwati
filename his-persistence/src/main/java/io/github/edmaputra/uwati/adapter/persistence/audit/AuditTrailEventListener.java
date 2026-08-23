package io.github.edmaputra.uwati.adapter.persistence.audit;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import io.github.edmaputra.uwati.core.audit.AuditDiffEngine;
import io.github.edmaputra.uwati.core.audit.AuditDiffEngine.CollectionDiff;
import io.github.edmaputra.uwati.core.audit.AuditDiffEngine.FieldDiff;
import io.github.edmaputra.uwati.core.audit.AuditJsonFormatter;
import io.github.edmaputra.uwati.domain.audit.AuditContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;
import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantCreated;
import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantSettingsUpdated;
import lombok.RequiredArgsConstructor;

/**
 * Listens to domain events and persists common, structured audit-trail entries.
 */
@Component
@RequiredArgsConstructor
public class AuditTrailEventListener {

	private final AuditEntryJpaRepository auditEntries;
	private final AuditContext auditContext;

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void onTenantCreated(TenantCreated event) {
		var tenant = event.tenant();

		Map<String, FieldDiff> fieldDiffs = AuditDiffEngine.diffFields(
				null,
				Map.of(
						"displayName", tenant.displayName(),
						"legalName", tenant.legalName(),
						"status", tenant.status().name()));

		String changesJson = AuditJsonFormatter.formatDiff(fieldDiffs);

		auditEntries.save(new AuditEntryEntity(
				tenant.id().value(),
				"Tenant",
				tenant.id().value().toString(),
				"CREATE",
				auditContext.requireActor(),
				auditContext.requireCorrelationId(),
				event.occurredAt(),
				changesJson));
	}

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void onTenantSettingsUpdated(TenantSettingsUpdated event) {
		CollectionDiff<TenantSetting> collectionDiff = AuditDiffEngine.diffKeyedCollection(
				event.previousSettings(),
				event.updatedSettings(),
				TenantSetting::key,
				(oldSetting, newSetting) -> AuditDiffEngine.diffFields(
						Map.of("value", oldSetting.value(), "revision", oldSetting.revision()),
						Map.of("value", newSetting.value(), "revision", newSetting.revision())));

		String changesJson = AuditJsonFormatter.formatCollectionDiff(
				"settings",
				collectionDiff,
				setting -> "{\"key\":\"%s\",\"value\":\"%s\",\"revision\":%d}".formatted(
						AuditJsonFormatter.escapeJson(setting.key()),
						AuditJsonFormatter.escapeJson(setting.value()),
						setting.revision()));

		auditEntries.save(new AuditEntryEntity(
				event.tenantId().value(),
				"TenantSetting",
				event.tenantId().value().toString(),
				"UPDATE",
				auditContext.requireActor(),
				auditContext.requireCorrelationId(),
				event.occurredAt(),
				changesJson));
	}
}
