package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.application.port.in.AssignGroupRoleCommand;
import io.github.edmaputra.uwati.iam.domain.model.GroupId;
import io.github.edmaputra.uwati.iam.domain.model.RoleId;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;

/**
 * REST request payload for assigning a role to a group.
 *
 * @param roleId          the role ID string
 * @param scopeNodeId     optional scope node ID string
 * @param inheritChildren whether permissions cascade downward
 * @author edmaputra
 */
public record AssignGroupRoleRequest(
		String roleId,
		String scopeNodeId,
		Boolean inheritChildren) {

	/**
	 * Converts this request DTO into a domain {@link AssignGroupRoleCommand}.
	 *
	 * @param tenantId the tenant ID
	 * @param groupId  the group ID
	 * @return the command record
	 */
	public AssignGroupRoleCommand toCommand(TenantId tenantId, GroupId groupId) {
		return new AssignGroupRoleCommand(
				tenantId,
				groupId,
				RoleId.from(roleId),
				scopeNodeId != null && !scopeNodeId.isBlank() ? ScopeNodeId.from(scopeNodeId) : null,
				inheritChildren == null || inheritChildren);
	}
}
