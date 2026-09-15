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
 * Transactional event listener that listens for domain lifecycle events and persists structured audit trails.
 * <p>
 * Acts as an inbound event handler adapter in the hexagonal architecture, observing
 * domain events published during business transactions and translating diffs into
 * persistent {@link AuditEntryEntity} records before commit.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class AuditTrailEventListener {

	private final AuditEntryJpaRepository auditEntries;

	/**
	 * Processes tenant creation events by capturing initial tenant state diffs.
	 *
	 * @param event domain event triggered upon tenant creation
	 */
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

	/**
	 * Processes tenant settings update events by persisting structured setting diffs.
	 *
	 * @param event domain event triggered when tenant settings are updated
	 */
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

	/**
	 * Processes healthcare facility creation events by recording the initial facility state.
	 *
	 * @param event domain event triggered upon facility creation
	 */
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

	/**
	 * Processes facility modification events by comparing previous and current states.
	 *
	 * @param event domain event triggered upon facility modification
	 */
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

	/**
	 * Processes facility operational status changes by recording old and new statuses.
	 *
	 * @param event domain event triggered upon facility status transition
	 */
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

	/**
	 * Processes service unit creation events by recording initial service unit state.
	 *
	 * @param event domain event triggered upon service unit creation
	 */
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

	/**
	 * Processes service unit modification events by computing differences between states.
	 *
	 * @param event domain event triggered upon service unit modification
	 */
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

	/**
	 * Processes service unit operational status changes by recording old and new statuses.
	 *
	 * @param event domain event triggered upon service unit status transition
	 */
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
