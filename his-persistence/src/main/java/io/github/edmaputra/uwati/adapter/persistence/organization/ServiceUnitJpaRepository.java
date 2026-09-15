package io.github.edmaputra.uwati.adapter.persistence.organization;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.github.edmaputra.uwati.domain.organization.ServiceUnitStatus;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitType;

/**
 * Spring Data JPA repository for service unit relational database operations.
 * <p>
 * Functions as an internal persistence mechanism in the hexagonal architecture's
 * outbound organization persistence adapter, providing low-level SQL and JPQL
 * query execution for service units within facilities.
 *
 * @author edmaputra
 * @since 0.0.1
 */
interface ServiceUnitJpaRepository extends JpaRepository<ServiceUnitEntity, UUID> {

	/**
	 * Finds a service unit entity by tenant ID and service unit ID.
	 *
	 * @param tenantId unique identifier of the tenant
	 * @param id unique identifier of the service unit
	 * @return an {@link Optional} containing the entity if found, or empty otherwise
	 */
	Optional<ServiceUnitEntity> findByTenantIdAndId(UUID tenantId, UUID id);

	/**
	 * Finds a service unit entity by tenant ID, facility ID, and code ignoring case.
	 *
	 * @param tenantId unique identifier of the tenant
	 * @param facilityId unique identifier of the parent facility
	 * @param code service unit code within the facility
	 * @return an {@link Optional} containing the entity if found, or empty otherwise
	 */
	Optional<ServiceUnitEntity> findByTenantIdAndFacilityIdAndCodeIgnoreCase(UUID tenantId, UUID facilityId, String code);

	/**
	 * Checks if a service unit exists with the given tenant ID, facility ID, and code ignoring case.
	 *
	 * @param tenantId unique identifier of the tenant
	 * @param facilityId unique identifier of the parent facility
	 * @param code service unit code within the facility
	 * @return {@code true} if an entity exists, {@code false} otherwise
	 */
	boolean existsByTenantIdAndFacilityIdAndCodeIgnoreCase(UUID tenantId, UUID facilityId, String code);

	/**
	 * Searches service units within a facility and tenant filtered optionally by type and status.
	 *
	 * @param tenantId unique identifier of the tenant
	 * @param facilityId unique identifier of the parent facility
	 * @param type optional service unit type filter
	 * @param status optional service unit status filter
	 * @return list of matching service unit entities
	 */
	@Query("SELECT s FROM ServiceUnitEntity s WHERE s.tenantId = :tenantId "
			+ "AND s.facilityId = :facilityId "
			+ "AND (:type IS NULL OR s.type = :type) "
			+ "AND (:status IS NULL OR s.status = :status) "
			+ "ORDER BY s.name ASC")
	List<ServiceUnitEntity> search(
			@Param("tenantId") UUID tenantId,
			@Param("facilityId") UUID facilityId,
			@Param("type") ServiceUnitType type,
			@Param("status") ServiceUnitStatus status);
}
