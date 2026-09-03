package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.application.port.in.AssignUserRoleCommand;
import io.github.edmaputra.uwati.iam.domain.model.RoleId;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;
import io.github.edmaputra.uwati.iam.domain.model.UserId;

/**
 * REST request payload for assigning a role to a user.
 *
 * @param roleId          the role ID to assign
 * @param tenantId        the tenant ID context
 * @param scopeNodeId     optional scope node ID
 * @param inheritChildren whether permissions cascade downward
 * @author edmaputra
 */
public record AssignUserRoleRequest(
		String roleId,
		String tenantId,
		String scopeNodeId,
		Boolean inheritChildren) {

	/**
	 * Converts this request DTO into a domain {@link AssignUserRoleCommand}.
	 *
	 * @param userId the target user ID
	 * @return the command record
	 */
	public AssignUserRoleCommand toCommand(UserId userId) {
		return new AssignUserRoleCommand(
				userId,
				RoleId.from(roleId),
				tenantId != null && !tenantId.isBlank() ? TenantId.from(tenantId) : null,
				scopeNodeId != null && !scopeNodeId.isBlank() ? ScopeNodeId.from(scopeNodeId) : null,
				inheritChildren == null || inheritChildren);
	}
}
