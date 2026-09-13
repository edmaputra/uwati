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

/**
 * Secondary (outbound) persistence adapter implementing {@link FacilityRepository} using Spring Data JPA.
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
public class JpaFacilityRepositoryAdapter implements FacilityRepository {

	private final FacilityJpaRepository facilities;
	private final TenantContext tenantContext;

	/**
	 * Persists a healthcare facility domain entity into relational storage.
	 *
	 * @param facility domain model representing the facility to persist
	 * @return the persisted facility domain model
	 * @throws NullPointerException if {@code facility} is null
	 */
	@Override
	public Facility save(Facility facility) {
		Objects.requireNonNull(facility, "Facility must not be null.");
		return toDomain(facilities.save(toEntity(facility)));
	}

	/**
	 * Finds a facility domain entity by its unique identifier within the active tenant.
	 *
	 * @param id unique identifier of the facility
	 * @return an {@link Optional} containing the facility if found, or empty if not found
	 * @throws NullPointerException if {@code id} is null
	 */
	@Override
	public Optional<Facility> findById(FacilityId id) {
		Objects.requireNonNull(id, "Facility ID must not be null.");
		TenantId tenantId = tenantContext.requireTenantId();
		return facilities.findByTenantIdAndId(tenantId.value(), id.value()).map(this::toDomain);
	}

	/**
	 * Finds a facility domain entity by its unique code within the active tenant.
	 *
	 * @param code unique facility code within the tenant
	 * @return an {@link Optional} containing the facility if found, or empty if not found or blank
	 */
	@Override
	public Optional<Facility> findByCode(String code) {
		if (code == null || code.isBlank()) {
			return Optional.empty();
		}
		TenantId tenantId = tenantContext.requireTenantId();
		return facilities.findByTenantIdAndCodeIgnoreCase(tenantId.value(), code.trim()).map(this::toDomain);
	}

	/**
	 * Checks if a facility exists with the specified code in the active tenant.
	 *
	 * @param code facility code to check
	 * @return {@code true} if a facility exists with the given code in the tenant, {@code false} otherwise
	 */
	@Override
	public boolean existsByCode(String code) {
		if (code == null || code.isBlank()) {
			return false;
		}
		TenantId tenantId = tenantContext.requireTenantId();
		return facilities.existsByTenantIdAndCodeIgnoreCase(tenantId.value(), code.trim());
	}

	/**
	 * Retrieves all facilities within the active tenant matching the specified type and status filters.
	 *
	 * @param type optional facility type filter
	 * @param status optional facility status filter
	 * @return list of matching facility domain models
	 */
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
