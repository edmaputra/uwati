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

@Component
@RequiredArgsConstructor
public class JpaServiceUnitRepositoryAdapter implements ServiceUnitRepository {

	private final ServiceUnitJpaRepository serviceUnits;
	private final TenantContext tenantContext;

	@Override
	public ServiceUnit save(ServiceUnit serviceUnit) {
		Objects.requireNonNull(serviceUnit, "Service unit must not be null.");
		return toDomain(serviceUnits.save(toEntity(serviceUnit)));
	}

	@Override
	public Optional<ServiceUnit> findById(ServiceUnitId id) {
		Objects.requireNonNull(id, "Service Unit ID must not be null.");
		TenantId tenantId = tenantContext.requireTenantId();
		return serviceUnits.findByTenantIdAndId(tenantId.value(), id.value()).map(this::toDomain);
	}

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

	@Override
	public boolean existsByFacilityIdAndCode(FacilityId facilityId, String code) {
		Objects.requireNonNull(facilityId, "Facility ID must not be null.");
		if (code == null || code.isBlank()) {
			return false;
		}
		TenantId tenantId = tenantContext.requireTenantId();
		return serviceUnits.existsByTenantIdAndFacilityIdAndCodeIgnoreCase(tenantId.value(), facilityId.value(), code.trim());
	}

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
