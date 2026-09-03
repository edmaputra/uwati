package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.application.port.in.CreateUserCommand;
import io.github.edmaputra.uwati.iam.domain.model.GroupId;
import io.github.edmaputra.uwati.iam.domain.model.RoleId;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;

/**
 * REST request payload for provisioning a new user account.
 *
 * @param email              the user email address
 * @param password           optional raw password
 * @param fullName           the user full name
 * @param isSuperAdmin       optional flag for platform superadmin
 * @param tenantId           optional initial tenant ID context
 * @param roleId             optional initial role ID
 * @param scopeNodeId        optional initial scope node ID
 * @param inheritChildren    whether role cascades downward
 * @param groupId            optional initial group ID
 * @author edmaputra
 */
public record CreateUserRequest(
		String email,
		String password,
		String fullName,
		Boolean isSuperAdmin,
		String tenantId,
		String roleId,
		String scopeNodeId,
		Boolean inheritChildren,
		String groupId) {

	/**
	 * Converts this request DTO into a domain {@link CreateUserCommand}.
	 *
	 * @return the command record
	 */
	public CreateUserCommand toCommand() {
		return new CreateUserCommand(
				email,
				password,
				fullName,
				Boolean.TRUE.equals(isSuperAdmin),
				tenantId != null && !tenantId.isBlank() ? TenantId.from(tenantId) : null,
				roleId != null && !roleId.isBlank() ? RoleId.from(roleId) : null,
				scopeNodeId != null && !scopeNodeId.isBlank() ? ScopeNodeId.from(scopeNodeId) : null,
				inheritChildren == null || inheritChildren,
				groupId != null && !groupId.isBlank() ? GroupId.from(groupId) : null);
	}
}
