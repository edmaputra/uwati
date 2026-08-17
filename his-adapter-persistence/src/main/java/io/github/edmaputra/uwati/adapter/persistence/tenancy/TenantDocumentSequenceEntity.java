package io.github.edmaputra.uwati.adapter.persistence.tenancy;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(
		name = "tenant_document_sequences",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_tenant_document_sequences_scope",
				columnNames = { "tenant_id", "document_type", "facility_scope" }),
		indexes = @Index(name = "idx_tenant_document_sequences_tenant_id", columnList = "tenant_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class TenantDocumentSequenceEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tenant_id", nullable = false, updatable = false)
	private UUID tenantId;

	@Column(name = "document_type", nullable = false)
	private String documentType;

	@Column(name = "facility_scope", nullable = false)
	private String facilityScope;

	@Column(nullable = false)
	private String prefix;

	@Column(name = "next_value", nullable = false)
	private long nextValue;

	@Column(name = "reset_policy", nullable = false)
	private String resetPolicy;

	TenantDocumentSequenceEntity(
			UUID tenantId,
			String documentType,
			String facilityScope,
			String prefix,
			long nextValue,
			String resetPolicy) {
		this.tenantId = tenantId;
		this.documentType = documentType;
		this.facilityScope = facilityScope;
		this.prefix = prefix;
		this.nextValue = nextValue;
		this.resetPolicy = resetPolicy;
	}
}
