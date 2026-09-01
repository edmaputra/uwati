package io.github.edmaputra.uwati.domain.audit;

import java.time.Instant;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;

/**
 * Domain model representing an immutable audit log entry.
 *
 * @author edmaputra
 */
public record AuditEntry(
		Long id,
		TenantId tenantId,
		String entityName,
		String entityId,
		String action,
		String actor,
		String correlationId,
		String changesJson,
		Instant occurredAt) {

	public AuditEntry {
		Objects.requireNonNull(entityName, "Entity name must not be null.");
		Objects.requireNonNull(entityId, "Entity ID must not be null.");
		Objects.requireNonNull(action, "Action must not be null.");
		Objects.requireNonNull(actor, "Actor must not be null.");
		Objects.requireNonNull(correlationId, "Correlation ID must not be null.");
		Objects.requireNonNull(changesJson, "Changes JSON must not be null.");
		Objects.requireNonNull(occurredAt, "Occurred-at timestamp must not be null.");
	}

	public static AuditEntry of(
			TenantId tenantId,
			String entityName,
			String entityId,
			String action,
			String actor,
			String correlationId,
			String changesJson) {
		return new AuditEntry(
				null,
				tenantId,
				entityName,
				entityId,
				action,
				actor,
				correlationId,
				changesJson,
				Instant.now());
	}
}
