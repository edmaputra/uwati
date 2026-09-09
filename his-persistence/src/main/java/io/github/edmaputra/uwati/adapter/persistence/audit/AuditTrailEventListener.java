package io.github.edmaputra.uwati.adapter.persistence.audit;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import io.github.edmaputra.uwati.core.audit.AuditDiffEngine;
import io.github.edmaputra.uwati.core.audit.AuditDiffEngine.CollectionDiff;
import io.github.edmaputra.uwati.core.audit.AuditDiffEngine.FieldDiff;
import io.github.edmaputra.uwati.core.audit.AuditJsonFormatter;
import io.github.edmaputra.uwati.domain.organization.event.FacilityCreated;
import io.github.edmaputra.uwati.domain.organization.event.FacilityStatusChanged;
import io.github.edmaputra.uwati.domain.organization.event.FacilityUpdated;
import io.github.edmaputra.uwati.domain.organization.event.ServiceUnitCreated;
import io.github.edmaputra.uwati.domain.organization.event.ServiceUnitStatusChanged;
import io.github.edmaputra.uwati.domain.organization.event.ServiceUnitUpdated;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;
import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantCreated;
import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantSettingsUpdated;
import lombok.RequiredArgsConstructor;

/**
 * Listens to domain events and persists common, structured audit-trail entries.
 * Uses the Auditable interface on domain models to capture only monitored fields.
 */
@Component
@RequiredArgsConstructor
public class AuditTrailEventListener {

	private final AuditEntryJpaRepository auditEntries;

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void onTenantCreated(TenantCreated event) {
		var tenant = event.tenant();

		Map<String, FieldDiff> fieldDiffs = AuditDiffEngine.diff(null, tenant);
		String changesJson = AuditJsonFormatter.formatDiff(fieldDiffs);

		auditEntries.save(new AuditEntryEntity(
				tenant.id().value(),
				"Tenant",
				tenant.id().value().toString(),
				"CREATE",
				event.actor(),
				event.correlationId() != null ? event.correlationId() : "unknown",
				event.occurredAt(),
				changesJson));
	}

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void onTenantSettingsUpdated(TenantSettingsUpdated event) {
		CollectionDiff<TenantSetting> collectionDiff = AuditDiffEngine.diffKeyedCollection(
				event.previousSettings(),
				event.updatedSettings(),
				TenantSetting::key);

		String changesJson = AuditJsonFormatter.formatCollectionDiff(
				"settings",
				collectionDiff,
				setting -> Map.of(
						"key", setting.key(),
						"value", setting.value(),
						"revision", setting.revision()));

		auditEntries.save(new AuditEntryEntity(
				event.tenantId().value(),
				"TenantSetting",
				event.tenantId().value().toString(),
				"UPDATE",
				event.actor(),
				event.correlationId() != null ? event.correlationId() : "unknown",
				event.occurredAt(),
				changesJson));
	}

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void onFacilityCreated(FacilityCreated event) {
		var facility = event.facility();
		Map<String, FieldDiff> diffs = AuditDiffEngine.diff(null, facility);
		String changesJson = AuditJsonFormatter.formatDiff(diffs);

		auditEntries.save(new AuditEntryEntity(
				facility.tenantId().value(),
				"Facility",
				facility.id().value().toString(),
				"CREATE",
				event.actor(),
				event.correlationId() != null ? event.correlationId() : "unknown",
				event.occurredAt(),
				changesJson));
	}

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void onFacilityUpdated(FacilityUpdated event) {
		Map<String, FieldDiff> diffs = AuditDiffEngine.diff(event.previous(), event.current());
		String changesJson = AuditJsonFormatter.formatDiff(diffs);

		auditEntries.save(new AuditEntryEntity(
				event.current().tenantId().value(),
				"Facility",
				event.current().id().value().toString(),
				"UPDATE",
				event.actor(),
				event.correlationId() != null ? event.correlationId() : "unknown",
				event.occurredAt(),
				changesJson));
	}

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void onFacilityStatusChanged(FacilityStatusChanged event) {
		var facility = event.facility();
		String changesJson = String.format("{\"status\":{\"old\":\"%s\",\"new\":\"%s\"}}",
				event.oldStatus().name(), event.newStatus().name());

		auditEntries.save(new AuditEntryEntity(
				facility.tenantId().value(),
				"Facility",
				facility.id().value().toString(),
				"STATUS_CHANGE",
				event.actor(),
				event.correlationId() != null ? event.correlationId() : "unknown",
				event.occurredAt(),
				changesJson));
	}

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void onServiceUnitCreated(ServiceUnitCreated event) {
		var serviceUnit = event.serviceUnit();
		Map<String, FieldDiff> diffs = AuditDiffEngine.diff(null, serviceUnit);
		String changesJson = AuditJsonFormatter.formatDiff(diffs);

		auditEntries.save(new AuditEntryEntity(
				serviceUnit.tenantId().value(),
				"ServiceUnit",
				serviceUnit.id().value().toString(),
				"CREATE",
				event.actor(),
				event.correlationId() != null ? event.correlationId() : "unknown",
				event.occurredAt(),
				changesJson));
	}

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void onServiceUnitUpdated(ServiceUnitUpdated event) {
		Map<String, FieldDiff> diffs = AuditDiffEngine.diff(event.previous(), event.current());
		String changesJson = AuditJsonFormatter.formatDiff(diffs);

		auditEntries.save(new AuditEntryEntity(
				event.current().tenantId().value(),
				"ServiceUnit",
				event.current().id().value().toString(),
				"UPDATE",
				event.actor(),
				event.correlationId() != null ? event.correlationId() : "unknown",
				event.occurredAt(),
				changesJson));
	}

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void onServiceUnitStatusChanged(ServiceUnitStatusChanged event) {
		var serviceUnit = event.serviceUnit();
		String changesJson = String.format("{\"status\":{\"old\":\"%s\",\"new\":\"%s\"}}",
				event.oldStatus().name(), event.newStatus().name());

		auditEntries.save(new AuditEntryEntity(
				serviceUnit.tenantId().value(),
				"ServiceUnit",
				serviceUnit.id().value().toString(),
				"STATUS_CHANGE",
				event.actor(),
				event.correlationId() != null ? event.correlationId() : "unknown",
				event.occurredAt(),
				changesJson));
	}
}
