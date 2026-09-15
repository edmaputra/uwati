package io.github.edmaputra.uwati.adapter.persistence.organization;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import io.github.edmaputra.uwati.domain.organization.FacilityClassification;
import io.github.edmaputra.uwati.domain.organization.FacilityStatus;
import io.github.edmaputra.uwati.domain.organization.FacilityType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * JPA entity mapping healthcare facility records to the relational database table.
 * <p>
 * Serves as an internal persistence data model in the hexagonal architecture's
 * outbound persistence adapter, isolating relational database schema concerns
 * from the {@link io.github.edmaputra.uwati.domain.organization.Facility} domain model.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@Entity
@Table(
		name = "facilities",
		uniqueConstraints = @UniqueConstraint(name = "uk_facilities_tenant_code", columnNames = { "tenant_id", "code" }))
@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class FacilityEntity {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(name = "tenant_id", nullable = false, updatable = false)
	private UUID tenantId;

	@Column(nullable = false, length = 50)
	private String code;

	@Column(nullable = false, length = 200)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private FacilityType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private FacilityClassification classification;

	@Column(name = "national_registry_code", length = 100)
	private String nationalRegistryCode;

	@Column(name = "scope_node_id")
	private UUID scopeNodeId;

	@Column(columnDefinition = "TEXT")
	private String address;

	@Column(length = 50)
	private String phone;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private FacilityStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}
