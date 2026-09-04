package io.github.edmaputra.uwati.iam.adapter.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.github.edmaputra.uwati.iam.adapter.persistence.entity.UserJpaEntity;

/**
 * Spring Data JPA repository for {@link UserJpaEntity}.
 *
 * @author edmaputra
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

	/**
	 * Finds a user entity by case-insensitive email.
	 *
	 * @param email the email address
	 * @return optional entity
	 */
	Optional<UserJpaEntity> findByEmailIgnoreCase(String email);

	/**
	 * Checks existence by case-insensitive email.
	 *
	 * @param email the email address
	 * @return true if exists
	 */
	boolean existsByEmailIgnoreCase(String email);

	/**
	 * Finds all users associated with a specific tenant via role assignments or group memberships.
	 *
	 * @param tenantId the tenant UUID
	 * @return list of user entities
	 */
	@Query("""
			SELECT DISTINCT u FROM UserJpaEntity u
			WHERE EXISTS (
				SELECT 1 FROM UserRoleAssignmentJpaEntity ura
				WHERE ura.userId = u.id AND ura.tenantId = :tenantId
			)
			OR EXISTS (
				SELECT 1 FROM UserGroupMembershipJpaEntity ugm, GroupJpaEntity g
				WHERE ugm.id.userId = u.id AND ugm.id.groupId = g.id AND g.tenantId = :tenantId
			)
			""")
	List<UserJpaEntity> findAllByTenantId(@Param("tenantId") UUID tenantId);
}

