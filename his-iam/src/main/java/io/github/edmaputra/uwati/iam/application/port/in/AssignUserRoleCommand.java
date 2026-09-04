package io.github.edmaputra.uwati.iam.application.port.in;

import java.util.Objects;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.domain.model.RoleId;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;
import io.github.edmaputra.uwati.iam.domain.model.UserId;

/**
 * Command for assigning a role to a user at a target tenant and optional scope node.
 *
 * @param userId          the user ID
 * @param roleId          the role ID to assign
 * @param tenantId        the tenant ID context (nullable for global superadmin)
 * @param scopeNodeId     optional scope node ID (nullable for tenant-wide access)
 * @param inheritChildren whether permissions cascade downward to descendant scopes
 * @author edmaputra
 */
public record AssignUserRoleCommand(
		UserId userId,
		RoleId roleId,
		TenantId tenantId,
		ScopeNodeId scopeNodeId,
		boolean inheritChildren) {

	public AssignUserRoleCommand {
		Objects.requireNonNull(userId, "UserId must not be null.");
		Objects.requireNonNull(roleId, "RoleId must not be null.");
	}
}
