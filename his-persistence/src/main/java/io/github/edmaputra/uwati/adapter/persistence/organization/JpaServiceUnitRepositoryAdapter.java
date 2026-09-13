package io.github.edmaputra.uwati.adapter.persistence.organization;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnit;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitStatus;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitType;
import io.github.edmaputra.uwati.domain.organization.port.out.ServiceUnitRepository;
import io.github.edmaputra.uwati.domain.tenancy.application.TenantContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import lombok.RequiredArgsConstructor;

/**
 * Secondary (outbound) persistence adapter implementing {@link ServiceUnitRepository} using Spring Data JPA.
 * <p>
 * Bridges domain-driven organization port interfaces to relational database persistence in the
 * hexagonal architecture. Enforces multi-tenant data isolation by scoping all queries
 * and persistence operations with the active {@link TenantContext}.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class JpaServiceUnitRepositoryAdapter implements ServiceUnitRepository {

	private final ServiceUnitJpaRepository serviceUnits;
	private final TenantContext tenantContext;

	/**
	 * Persists a service unit domain entity into relational storage.
	 *
	 * @param serviceUnit domain model representing the service unit to persist
	 * @return the persisted service unit domain model
	 * @throws NullPointerException if {@code serviceUnit} is null
	 */
	@Override
	public ServiceUnit save(ServiceUnit serviceUnit) {
		Objects.requireNonNull(serviceUnit, "Service unit must not be null.");
		return toDomain(serviceUnits.save(toEntity(serviceUnit)));
	}

	/**
	 * Finds a service unit domain entity by its unique identifier within the active tenant.
	 *
	 * @param id unique identifier of the service unit
	 * @return an {@link Optional} containing the service unit if found, or empty if not found
	 * @throws NullPointerException if {@code id} is null
	 */
	@Override
	public Optional<ServiceUnit> findById(ServiceUnitId id) {
		Objects.requireNonNull(id, "Service Unit ID must not be null.");
		TenantId tenantId = tenantContext.requireTenantId();
		return serviceUnits.findByTenantIdAndId(tenantId.value(), id.value()).map(this::toDomain);
	}

	/**
	 * Finds a service unit domain entity by parent facility ID and code within the active tenant.
	 *
	 * @param facilityId parent facility identifier
	 * @param code service unit code to search for within the facility
	 * @return an {@link Optional} containing the service unit if found, or empty if not found or blank
	 * @throws NullPointerException if {@code facilityId} is null
	 */
	@Override
	public Optional<ServiceUnit> findByFacilityIdAndCode(FacilityId facilityId, String code) {
		Objects.requireNonNull(facilityId, "Facility ID must not be null.");
		if (code == null || code.isBlank()) {
			return Optional.empty();
		}
		TenantId tenantId = tenantContext.requireTenantId();
		return serviceUnits.findByTenantIdAndFacilityIdAndCodeIgnoreCase(tenantId.value(), facilityId.value(), code.trim())
				.map(this::toDomain);
	}

	/**
	 * Checks if a service unit exists with the specified code in the given facility under the active tenant.
	 *
	 * @param facilityId parent facility identifier
	 * @param code service unit code to check
	 * @return {@code true} if a service unit exists with the given code in the facility, {@code false} otherwise
	 * @throws NullPointerException if {@code facilityId} is null
	 */
	@Override
	public boolean existsByFacilityIdAndCode(FacilityId facilityId, String code) {
		Objects.requireNonNull(facilityId, "Facility ID must not be null.");
		if (code == null || code.isBlank()) {
			return false;
		}
		TenantId tenantId = tenantContext.requireTenantId();
		return serviceUnits.existsByTenantIdAndFacilityIdAndCodeIgnoreCase(tenantId.value(), facilityId.value(), code.trim());
	}

	/**
	 * Retrieves all service units within a facility matching optional type and status filters.
	 *
	 * @param facilityId parent facility identifier
	 * @param type optional service unit type filter
	 * @param status optional service unit status filter
	 * @return list of matching service unit domain models
	 * @throws NullPointerException if {@code facilityId} is null
	 */
	@Override
	public List<ServiceUnit> findByFacilityId(FacilityId facilityId, ServiceUnitType type, ServiceUnitStatus status) {
		Objects.requireNonNull(facilityId, "Facility ID must not be null.");
		TenantId tenantId = tenantContext.requireTenantId();
		return serviceUnits.search(tenantId.value(), facilityId.value(), type, status).stream().map(this::toDomain).toList();
	}

	private ServiceUnit toDomain(ServiceUnitEntity entity) {
		return new ServiceUnit(
				new ServiceUnitId(entity.id()),
				new TenantId(entity.tenantId()),
				new FacilityId(entity.facilityId()),
				entity.code(),
				entity.name(),
				entity.type(),
				entity.scopeNodeId(),
				entity.status(),
				entity.createdAt(),
				entity.updatedAt());
	}

	private ServiceUnitEntity toEntity(ServiceUnit domain) {
		return new ServiceUnitEntity(
				domain.id().value(),
				domain.tenantId().value(),
				domain.facilityId().value(),
				domain.code(),
				domain.name(),
				domain.type(),
				domain.scopeNodeId(),
				domain.status(),
				domain.createdAt(),
				domain.updatedAt());
	}
}
