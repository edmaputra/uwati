package io.github.edmaputra.uwati.iam.application.port.in;

import java.util.List;

import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.application.model.EffectiveAccess;
import io.github.edmaputra.uwati.iam.domain.model.User;
import io.github.edmaputra.uwati.iam.domain.model.UserId;
import io.github.edmaputra.uwati.iam.domain.model.UserIdentity;
import io.github.edmaputra.uwati.iam.domain.model.UserIdentityId;
import io.github.edmaputra.uwati.iam.domain.model.UserRoleAssignment;
import io.github.edmaputra.uwati.iam.domain.model.UserRoleAssignmentId;

/**
 * Inbound port for managing user lifecycles, profile updates, status transitions,
 * passwords, federated identity links, and direct role assignments.
 *
 * @author edmaputra
 */
public interface ManageUserUseCase {

	/**
	 * Provisions a new user account with optional initial role and group assignments.
	 *
	 * @param command the creation command
	 * @param context the operation context
	 * @return the created {@link User}
	 */
	User createUser(CreateUserCommand command, OperationContext context);

	/**
	 * Retrieves a user by their unique identifier.
	 *
	 * @param id the user ID
	 * @return the matching {@link User}
	 */
	User getUserById(UserId id);

	/**
	 * Updates a user's profile metadata.
	 *
	 * @param command the profile update command
	 * @param context the operation context
	 * @return the updated {@link User}
	 */
	User updateUserProfile(UpdateUserProfileCommand command, OperationContext context);

	/**
	 * Transitions a user's lifecycle state (ACTIVE, SUSPENDED, DEACTIVATED).
	 *
	 * @param command the status change command
	 * @param context the operation context
	 * @return the updated {@link User}
	 */
	User changeUserStatus(ChangeUserStatusCommand command, OperationContext context);

	/**
	 * Updates or resets a user's password.
	 *
	 * @param command the password update command
	 * @param context the operation context
	 */
	void updatePassword(UpdatePasswordCommand command, OperationContext context);

	/**
	 * Deactivates or removes a user.
	 *
	 * @param command the deletion command
	 * @param context the operation context
	 */
	void deleteUser(DeleteUserCommand command, OperationContext context);

	/**
	 * Calculates and compiles the effective roles, permissions, and scopes for a user in a tenant.
	 *
	 * @param userId   the user ID
	 * @param tenantId the tenant ID
	 * @return compiled {@link EffectiveAccess}
	 */
	EffectiveAccess getUserEffectiveAccess(UserId userId, TenantId tenantId);

	/**
	 * Retrieves all federated SSO identities linked to a user.
	 *
	 * @param userId the user ID
	 * @return list of linked {@link UserIdentity}
	 */
	List<UserIdentity> getUserIdentities(UserId userId);

	/**
	 * Unlinks a federated identity account from a user.
	 *
	 * @param userId     the user ID
	 * @param identityId the identity record ID to remove
	 * @param context    the operation context
	 */
	void unlinkUserIdentity(UserId userId, UserIdentityId identityId, OperationContext context);

	/**
	 * Retrieves all direct role assignments for a user within a tenant.
	 *
	 * @param userId   the user ID
	 * @param tenantId the tenant ID
	 * @return list of direct {@link UserRoleAssignment}
	 */
	List<UserRoleAssignment> getUserRoleAssignments(UserId userId, TenantId tenantId);

	/**
	 * Assigns a role to a user at a target tenant and optional scope node.
	 *
	 * @param command the role assignment command
	 * @param context the operation context
	 * @return the created {@link UserRoleAssignment}
	 */
	UserRoleAssignment assignRoleToUser(AssignUserRoleCommand command, OperationContext context);

	/**
	 * Revokes a direct role assignment from a user.
	 *
	 * @param assignmentId the assignment ID to revoke
	 * @param context      the operation context
	 */
	void revokeUserRoleAssignment(UserRoleAssignmentId assignmentId, OperationContext context);

	/**
	 * Searches and lists users within a tenant matching query criteria.
	 *
	 * @param tenantId optional tenant ID filter
	 * @param query    the search and filter criteria
	 * @return list of matching {@link User} entities
	 */
	List<User> listUsers(TenantId tenantId, UserQuery query);
}
