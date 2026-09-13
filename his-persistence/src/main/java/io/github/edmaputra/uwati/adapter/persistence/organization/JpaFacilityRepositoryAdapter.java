package io.github.edmaputra.uwati.adapter.persistence.organization;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import io.github.edmaputra.uwati.domain.organization.Facility;
import io.github.edmaputra.uwati.domain.organization.FacilityClassification;
import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.FacilityStatus;
import io.github.edmaputra.uwati.domain.organization.FacilityType;
import io.github.edmaputra.uwati.domain.organization.port.out.FacilityRepository;
import io.github.edmaputra.uwati.domain.tenancy.application.TenantContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JpaFacilityRepositoryAdapter implements FacilityRepository {

	private final FacilityJpaRepository facilities;
	private final TenantContext tenantContext;

	@Override
	public Facility save(Facility facility) {
		Objects.requireNonNull(facility, "Facility must not be null.");
		return toDomain(facilities.save(toEntity(facility)));
	}

	@Override
	public Optional<Facility> findById(FacilityId id) {
		Objects.requireNonNull(id, "Facility ID must not be null.");
		TenantId tenantId = tenantContext.requireTenantId();
		return facilities.findByTenantIdAndId(tenantId.value(), id.value()).map(this::toDomain);
	}

	@Override
	public Optional<Facility> findByCode(String code) {
		if (code == null || code.isBlank()) {
			return Optional.empty();
		}
		TenantId tenantId = tenantContext.requireTenantId();
		return facilities.findByTenantIdAndCodeIgnoreCase(tenantId.value(), code.trim()).map(this::toDomain);
	}

	@Override
	public boolean existsByCode(String code) {
		if (code == null || code.isBlank()) {
			return false;
		}
		TenantId tenantId = tenantContext.requireTenantId();
		return facilities.existsByTenantIdAndCodeIgnoreCase(tenantId.value(), code.trim());
	}

	@Override
	public List<Facility> findAll(FacilityType type, FacilityStatus status) {
		TenantId tenantId = tenantContext.requireTenantId();
		return facilities.search(tenantId.value(), type, status).stream().map(this::toDomain).toList();
	}

	private Facility toDomain(FacilityEntity entity) {
		return new Facility(
				new FacilityId(entity.id()),
				new TenantId(entity.tenantId()),
				entity.code(),
				entity.name(),
				entity.type(),
				entity.classification(),
				entity.nationalRegistryCode(),
				entity.scopeNodeId(),
				entity.address(),
				entity.phone(),
				entity.status(),
				entity.createdAt(),
				entity.updatedAt());
	}

	private FacilityEntity toEntity(Facility domain) {
		return new FacilityEntity(
				domain.id().value(),
				domain.tenantId().value(),
				domain.code(),
				domain.name(),
				domain.type(),
				domain.classification(),
				domain.nationalRegistryCode(),
				domain.scopeNodeId(),
				domain.address(),
				domain.phone(),
				domain.status(),
				domain.createdAt(),
				domain.updatedAt());
	}
}
