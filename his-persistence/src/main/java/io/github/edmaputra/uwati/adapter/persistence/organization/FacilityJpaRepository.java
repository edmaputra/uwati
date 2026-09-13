package io.github.edmaputra.uwati.adapter.persistence.organization;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.github.edmaputra.uwati.domain.organization.FacilityClassification;
import io.github.edmaputra.uwati.domain.organization.FacilityStatus;
import io.github.edmaputra.uwati.domain.organization.FacilityType;

/**
 * Spring Data JPA repository for facility relational database operations.
 * <p>
 * Functions as an internal persistence mechanism in the hexagonal architecture's
 * outbound organization persistence adapter, providing low-level SQL and JPQL
 * query execution for facilities.
 *
 * @author edmaputra
 * @since 0.0.1
 */
interface FacilityJpaRepository extends JpaRepository<FacilityEntity, UUID> {

	/**
	 * Finds a facility entity by tenant ID and facility ID.
	 *
	 * @param tenantId unique identifier of the tenant
	 * @param id unique identifier of the facility
	 * @return an {@link Optional} containing the entity if found, or empty otherwise
	 */
	Optional<FacilityEntity> findByTenantIdAndId(UUID tenantId, UUID id);

	/**
	 * Finds a facility entity by tenant ID and code ignoring case.
	 *
	 * @param tenantId unique identifier of the tenant
	 * @param code unique facility code within the tenant
	 * @return an {@link Optional} containing the entity if found, or empty otherwise
	 */
	Optional<FacilityEntity> findByTenantIdAndCodeIgnoreCase(UUID tenantId, String code);

	/**
	 * Checks if a facility exists with the given tenant ID and code ignoring case.
	 *
	 * @param tenantId unique identifier of the tenant
	 * @param code unique facility code within the tenant
	 * @return {@code true} if an entity exists, {@code false} otherwise
	 */
	boolean existsByTenantIdAndCodeIgnoreCase(UUID tenantId, String code);

	/**
	 * Searches facilities within a tenant filtered optionally by type and status.
	 *
	 * @param tenantId unique identifier of the tenant
	 * @param type optional facility type filter
	 * @param status optional facility status filter
	 * @return list of matching facility entities
	 */
	@Query("SELECT f FROM FacilityEntity f WHERE f.tenantId = :tenantId "
			+ "AND (:type IS NULL OR f.type = :type) "
			+ "AND (:status IS NULL OR f.status = :status) "
			+ "ORDER BY f.name ASC")
	List<FacilityEntity> search(
			@Param("tenantId") UUID tenantId,
			@Param("type") FacilityType type,
			@Param("status") FacilityStatus status);
}
