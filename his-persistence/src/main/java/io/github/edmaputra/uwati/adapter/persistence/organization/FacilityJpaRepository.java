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

interface FacilityJpaRepository extends JpaRepository<FacilityEntity, UUID> {

	Optional<FacilityEntity> findByTenantIdAndId(UUID tenantId, UUID id);

	Optional<FacilityEntity> findByTenantIdAndCodeIgnoreCase(UUID tenantId, String code);

	boolean existsByTenantIdAndCodeIgnoreCase(UUID tenantId, String code);

	@Query("SELECT f FROM FacilityEntity f WHERE f.tenantId = :tenantId "
			+ "AND (:type IS NULL OR f.type = :type) "
			+ "AND (:status IS NULL OR f.status = :status) "
			+ "ORDER BY f.name ASC")
	List<FacilityEntity> search(
			@Param("tenantId") UUID tenantId,
			@Param("type") FacilityType type,
			@Param("status") FacilityStatus status);
}
