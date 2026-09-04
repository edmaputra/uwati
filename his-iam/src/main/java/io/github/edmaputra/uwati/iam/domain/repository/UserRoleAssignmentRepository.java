package io.github.edmaputra.uwati.iam.domain.repository;

import java.util.List;
import java.util.Optional;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.domain.model.RoleId;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;
import io.github.edmaputra.uwati.iam.domain.model.UserId;
import io.github.edmaputra.uwati.iam.domain.model.UserRoleAssignment;
import io.github.edmaputra.uwati.iam.domain.model.UserRoleAssignmentId;

/**
 * Domain repository port for managing direct {@link UserRoleAssignment} bindings.
 *
 * @author edmaputra
 */
public interface UserRoleAssignmentRepository {

	/**
	 * Finds a user role assignment by unique identifier.
	 *
	 * @param id the assignment ID
	 * @return optional containing the assignment if found
	 */
	Optional<UserRoleAssignment> findById(UserRoleAssignmentId id);

	/**
	 * Finds all role assignments for a user across all tenants/scopes.
	 *
	 * @param userId the user ID
	 * @return list of user role assignments
	 */
	List<UserRoleAssignment> findAllByUserId(UserId userId);

	/**
	 * Finds all role assignments for a user within a specific tenant.
	 *
	 * @param userId   the user ID
	 * @param tenantId the tenant ID
	 * @return list of role assignments
	 */
	List<UserRoleAssignment> findAllByUserIdAndTenantId(UserId userId, TenantId tenantId);

	/**
	 * Checks if any user role assignment references the specified role ID.
	 *
	 * @param roleId the role ID
	 * @return true if an assignment exists
	 */
	boolean existsByRoleId(RoleId roleId);

	/**
	 * Checks if any user role assignment references the specified scope node ID.
	 *
	 * @param scopeNodeId the scope node ID
	 * @return true if an assignment exists
	 */
	boolean existsByScopeNodeId(ScopeNodeId scopeNodeId);

	/**
	 * Saves a user role assignment.
	 *
	 * @param assignment the assignment to persist
	 * @return the persisted assignment
	 */
	UserRoleAssignment save(UserRoleAssignment assignment);

	/**
	 * Deletes a user role assignment by unique identifier.
	 *
	 * @param id the assignment ID
	 */
	void delete(UserRoleAssignmentId id);

	/**
	 * Deletes all role assignments for a user.
	 *
	 * @param userId the user ID
	 */
	void deleteAllByUserId(UserId userId);
}

