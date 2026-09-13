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

import io.github.edmaputra.uwati.domain.organization.ServiceUnitStatus;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@Table(
		name = "service_units",
		uniqueConstraints = @UniqueConstraint(name = "uk_service_units_tenant_facility_code", columnNames = { "tenant_id", "facility_id", "code" }))
@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class ServiceUnitEntity {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(name = "tenant_id", nullable = false, updatable = false)
	private UUID tenantId;

	@Column(name = "facility_id", nullable = false, updatable = false)
	private UUID facilityId;

	@Column(nullable = false, length = 50)
	private String code;

	@Column(nullable = false, length = 200)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private ServiceUnitType type;

	@Column(name = "scope_node_id")
	private UUID scopeNodeId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ServiceUnitStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}
