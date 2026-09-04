package io.github.edmaputra.uwati.iam.domain.repository;

import java.util.List;
import java.util.Optional;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.domain.model.GroupId;
import io.github.edmaputra.uwati.iam.domain.model.GroupRoleAssignment;
import io.github.edmaputra.uwati.iam.domain.model.GroupRoleAssignmentId;

/**
 * Domain repository port for managing group-level {@link GroupRoleAssignment} bindings.
 *
 * @author edmaputra
 */
public interface GroupRoleAssignmentRepository {

	/**
	 * Finds a group role assignment by unique identifier.
	 *
	 * @param id the assignment ID
	 * @return optional containing the assignment if found
	 */
	Optional<GroupRoleAssignment> findById(GroupRoleAssignmentId id);

	/**
	 * Finds all role assignments for a group.
	 *
	 * @param groupId the group ID
	 * @return list of group role assignments
	 */
	List<GroupRoleAssignment> findAllByGroupId(GroupId groupId);

	/**
	 * Finds all role assignments for a collection of groups.
	 *
	 * @param groupIds iterable of group IDs
	 * @return list of group role assignments
	 */
	List<GroupRoleAssignment> findAllByGroupIds(Iterable<GroupId> groupIds);

	/**
	 * Finds all group role assignments within a tenant.
	 *
	 * @param tenantId the tenant ID
	 * @return list of group role assignments
	 */
	List<GroupRoleAssignment> findAllByTenantId(TenantId tenantId);

	/**
	 * Checks if any group role assignment references the specified role ID.
	 *
	 * @param roleId the role ID
	 * @return true if an assignment exists
	 */
	boolean existsByRoleId(io.github.edmaputra.uwati.iam.domain.model.RoleId roleId);

	/**
	 * Checks if any group role assignment references the specified scope node ID.
	 *
	 * @param scopeNodeId the scope node ID
	 * @return true if an assignment exists
	 */
	boolean existsByScopeNodeId(io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId scopeNodeId);

	/**
	 * Saves a group role assignment.
	 *
	 * @param assignment the assignment to persist
	 * @return the persisted assignment
	 */
	GroupRoleAssignment save(GroupRoleAssignment assignment);

	/**
	 * Deletes a group role assignment by unique identifier.
	 *
	 * @param id the assignment ID
	 */
	void delete(GroupRoleAssignmentId id);

	/**
	 * Deletes all role assignments for a group.
	 *
	 * @param groupId the group ID
	 */
	void deleteAllByGroupId(GroupId groupId);
}
