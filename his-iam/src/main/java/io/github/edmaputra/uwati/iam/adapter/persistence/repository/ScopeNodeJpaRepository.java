package io.github.edmaputra.uwati.iam.adapter.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.github.edmaputra.uwati.iam.adapter.persistence.entity.ScopeNodeEntity;

public interface ScopeNodeJpaRepository extends JpaRepository<ScopeNodeEntity, UUID> {

	List<ScopeNodeEntity> findAllByTenantId(UUID tenantId);

	Optional<ScopeNodeEntity> findByTenantIdAndCode(UUID tenantId, String code);

	List<ScopeNodeEntity> findByParentId(UUID parentId);

	List<ScopeNodeEntity> findByPathStartingWith(String pathPrefix);

	boolean existsByParentId(UUID parentId);

	boolean existsByTenantIdAndCode(UUID tenantId, String code);

	@Modifying
	@Query("UPDATE ScopeNodeJpaEntity s SET s.path = CONCAT(:newPrefix, SUBSTRING(s.path, LENGTH(:oldPrefix) + 1)), s.updatedAt = :now WHERE s.path LIKE CONCAT(:oldPrefix, '%')")
	int updatePathPrefix(
			@Param("oldPrefix") String oldPrefix,
			@Param("newPrefix") String newPrefix,
			@Param("now") Instant now);
}
