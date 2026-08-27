package io.github.edmaputra.uwati.iam.adapter.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.github.edmaputra.uwati.iam.adapter.persistence.entity.ScopeNodeJpaEntity;

public interface SpringDataScopeNodeRepository extends JpaRepository<ScopeNodeJpaEntity, UUID> {

	List<ScopeNodeJpaEntity> findAllByTenantId(UUID tenantId);

	Optional<ScopeNodeJpaEntity> findByTenantIdAndCode(UUID tenantId, String code);

	List<ScopeNodeJpaEntity> findByParentId(UUID parentId);

	List<ScopeNodeJpaEntity> findByPathStartingWith(String pathPrefix);

	boolean existsByParentId(UUID parentId);

	boolean existsByTenantIdAndCode(UUID tenantId, String code);

	@Modifying
	@Query("UPDATE ScopeNodeJpaEntity s SET s.path = CONCAT(:newPrefix, SUBSTRING(s.path, LENGTH(:oldPrefix) + 1)), s.updatedAt = :now WHERE s.path LIKE CONCAT(:oldPrefix, '%')")
	int updatePathPrefix(
			@Param("oldPrefix") String oldPrefix,
			@Param("newPrefix") String newPrefix,
			@Param("now") Instant now);
}
