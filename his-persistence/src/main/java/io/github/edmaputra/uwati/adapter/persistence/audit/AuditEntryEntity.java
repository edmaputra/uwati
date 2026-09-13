package io.github.edmaputra.uwati.adapter.persistence.audit;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing an immutable audit log entry in the relational database.
 * <p>
 * Acts as the persistence data model for system audit trails in the hexagonal
 * architecture's outbound persistence layer, capturing structured entity mutations
 * and correlation metadata.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@Entity
@Table(name = "audit_entries", indexes = {
		@Index(name = "idx_audit_entries_entity", columnList = "entity_name, entity_id"),
		@Index(name = "idx_audit_entries_tenant_id", columnList = "tenant_id"),
		@Index(name = "idx_audit_entries_correlation_id", columnList = "correlation_id"),
		@Index(name = "idx_audit_entries_occurred_at", columnList = "occurred_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditEntryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tenant_id")
	private UUID tenantId;

	@Column(name = "entity_name", nullable = false)
	private String entityName;

	@Column(name = "entity_id", nullable = false)
	private String entityId;

	@Column(name = "action", nullable = false)
	private String action;

	@Column(name = "actor", nullable = false)
	private String actor;

	@Column(name = "correlation_id", nullable = false)
	private String correlationId;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;

	@Column(name = "changes_json", nullable = false, columnDefinition = "text")
	private String changesJson;

	/**
	 * Constructs a new audit entry entity with captured metadata and serialized mutation diffs.
	 *
	 * @param tenantId optional tenant identifier associated with the audit record
	 * @param entityName name of the audited domain entity
	 * @param entityId unique identifier of the audited entity
	 * @param action type of action performed (e.g. CREATE, UPDATE, STATUS_CHANGE)
	 * @param actor identity of user or system performing the action
	 * @param correlationId tracking identifier for distributed tracing
	 * @param occurredAt timestamp when the event occurred
	 * @param changesJson JSON representation of the changes captured
	 */
	public AuditEntryEntity(
			UUID tenantId,
			String entityName,
			String entityId,
			String action,
			String actor,
			String correlationId,
			Instant occurredAt,
			String changesJson) {
		this.tenantId = tenantId;
		this.entityName = entityName;
		this.entityId = entityId;
		this.action = action;
		this.actor = actor;
		this.correlationId = correlationId;
		this.occurredAt = occurredAt;
		this.changesJson = changesJson;
	}
}
