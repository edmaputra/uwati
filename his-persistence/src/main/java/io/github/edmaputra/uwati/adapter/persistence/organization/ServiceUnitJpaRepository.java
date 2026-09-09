package io.github.edmaputra.uwati.adapter.persistence.organization;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.github.edmaputra.uwati.domain.organization.ServiceUnitStatus;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitType;

interface ServiceUnitJpaRepository extends JpaRepository<ServiceUnitEntity, UUID> {

	Optional<ServiceUnitEntity> findByTenantIdAndId(UUID tenantId, UUID id);

	Optional<ServiceUnitEntity> findByTenantIdAndFacilityIdAndCodeIgnoreCase(UUID tenantId, UUID facilityId, String code);

	boolean existsByTenantIdAndFacilityIdAndCodeIgnoreCase(UUID tenantId, UUID facilityId, String code);

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
