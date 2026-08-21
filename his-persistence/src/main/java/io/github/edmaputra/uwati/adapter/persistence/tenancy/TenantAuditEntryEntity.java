package io.github.edmaputra.uwati.adapter.persistence.tenancy;

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
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tenant_audit_entries", indexes = @Index(name = "idx_tenant_audit_entries_tenant_id", columnList = "tenant_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class TenantAuditEntryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tenant_id", nullable = false, updatable = false)
	private UUID tenantId;

	@Column(name = "event_type", nullable = false)
	private String eventType;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;

	@Column(name = "details_json", nullable = false, columnDefinition = "text")
	private String detailsJson;

	TenantAuditEntryEntity(UUID tenantId, String eventType, Instant occurredAt, String detailsJson) {
		this.tenantId = tenantId;
		this.eventType = eventType;
		this.occurredAt = occurredAt;
		this.detailsJson = detailsJson;
	}
}
