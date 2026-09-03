package io.github.edmaputra.uwati.iam.application.port.in;

import java.util.List;

import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.domain.model.Group;
import io.github.edmaputra.uwati.iam.domain.model.GroupId;
import io.github.edmaputra.uwati.iam.domain.model.GroupRoleAssignment;
import io.github.edmaputra.uwati.iam.domain.model.GroupRoleAssignmentId;
import io.github.edmaputra.uwati.iam.domain.model.User;

/**
 * Inbound port for managing user groups, team memberships, and group-level role assignments.
 *
 * @author edmaputra
 */
public interface ManageGroupUseCase {

	/**
	 * Creates a new user group within a tenant.
	 *
	 * @param command the group creation command
	 * @param context the operation context
	 * @return the created {@link Group}
	 */
	Group createGroup(CreateGroupCommand command, OperationContext context);

	/**
	 * Retrieves a group by ID within a tenant.
	 *
	 * @param tenantId the tenant ID
	 * @param id       the group ID
	 * @return the matching {@link Group}
	 */
	Group getGroupById(TenantId tenantId, GroupId id);

	/**
	 * Updates a group's metadata and SSO claim mapping.
	 *
	 * @param command the update command
	 * @param context the operation context
	 * @return the updated {@link Group}
	 */
	Group updateGroup(UpdateGroupCommand command, OperationContext context);

	/**
	 * Deletes a group and cascades removal of memberships and group role assignments.
	 *
	 * @param command the delete command
	 * @param context the operation context
	 */
	void deleteGroup(DeleteGroupCommand command, OperationContext context);

	/**
	 * Lists all groups for a tenant.
	 *
	 * @param tenantId the tenant ID
	 * @return list of {@link Group} entities
	 */
	List<Group> listGroups(TenantId tenantId);

	/**
	 * Retrieves all member users belonging to a group.
	 *
	 * @param tenantId the tenant ID
	 * @param groupId  the group ID
	 * @return list of member {@link User} entities
	 */
	List<User> getGroupMembers(TenantId tenantId, GroupId groupId);

	/**
	 * Adds a user to a group.
	 *
	 * @param command the add member command
	 * @param context the operation context
	 */
	void addGroupMember(AddGroupMemberCommand command, OperationContext context);

	/**
	 * Removes a user from a group.
	 *
	 * @param command the remove member command
	 * @param context the operation context
	 */
	void removeGroupMember(RemoveGroupMemberCommand command, OperationContext context);

	/**
	 * Retrieves all role assignments bound to a group.
	 *
	 * @param tenantId the tenant ID
	 * @param groupId  the group ID
	 * @return list of {@link GroupRoleAssignment}
	 */
	List<GroupRoleAssignment> getGroupRoleAssignments(TenantId tenantId, GroupId groupId);

	/**
	 * Assigns a role to a group at a target tenant and optional scope node.
	 *
	 * @param command the assignment command
	 * @param context the operation context
	 * @return the created {@link GroupRoleAssignment}
	 */
	GroupRoleAssignment assignRoleToGroup(AssignGroupRoleCommand command, OperationContext context);

	/**
	 * Revokes a role assignment from a group.
	 *
	 * @param assignmentId the assignment ID to revoke
	 * @param context      the operation context
	 */
	void revokeGroupRoleAssignment(GroupRoleAssignmentId assignmentId, OperationContext context);
}
