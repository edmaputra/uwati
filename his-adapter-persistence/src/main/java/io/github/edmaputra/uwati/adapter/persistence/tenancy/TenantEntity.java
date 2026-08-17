package io.github.edmaputra.uwati.adapter.persistence.tenancy;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import io.github.edmaputra.uwati.core.tenancy.domain.TenantStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@Table(
		name = "tenants",
		uniqueConstraints = @UniqueConstraint(name = "uk_tenants_display_name_normalized", columnNames = "display_name_normalized"))
@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class TenantEntity {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(name = "legal_name", nullable = false)
	private String legalName;

	@Column(name = "display_name", nullable = false)
	private String displayName;

	@Column(name = "display_name_normalized", nullable = false)
	private String displayNameNormalized;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TenantStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}
