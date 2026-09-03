package io.github.edmaputra.uwati.iam.application.port.in;

import java.util.Objects;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.domain.model.GroupId;
import io.github.edmaputra.uwati.iam.domain.model.RoleId;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;

/**
 * Command for assigning a role to a group at a target tenant and optional scope node.
 *
 * @param tenantId        the owning tenant ID
 * @param groupId         the group ID
 * @param roleId          the role ID to assign
 * @param scopeNodeId     optional scope node ID (nullable for tenant-wide access)
 * @param inheritChildren whether permissions cascade downward to descendant scopes
 * @author edmaputra
 */
public record AssignGroupRoleCommand(
		TenantId tenantId,
		GroupId groupId,
		RoleId roleId,
		ScopeNodeId scopeNodeId,
		boolean inheritChildren) {

	public AssignGroupRoleCommand {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		Objects.requireNonNull(groupId, "GroupId must not be null.");
		Objects.requireNonNull(roleId, "RoleId must not be null.");
	}
}
