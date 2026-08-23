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
