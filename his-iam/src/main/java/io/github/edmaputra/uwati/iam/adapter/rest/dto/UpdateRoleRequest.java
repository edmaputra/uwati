package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import java.util.Set;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.application.port.in.UpdateRoleCommand;
import io.github.edmaputra.uwati.iam.domain.model.RoleId;

/**
 * REST request payload for updating a role's name, description, and permissions.
 *
 * @param name        the updated name
 * @param description the updated description
 * @param permissions the updated set of permission keys
 * @author edmaputra
 */
public record UpdateRoleRequest(
		String name,
		String description,
		Set<String> permissions) {

	/**
	 * Converts this request DTO into a domain {@link UpdateRoleCommand}.
	 *
	 * @param tenantId optional tenant ID context
	 * @param roleId   the role ID to update
	 * @return the command record
	 */
	public UpdateRoleCommand toCommand(TenantId tenantId, RoleId roleId) {
		return new UpdateRoleCommand(
				tenantId,
				roleId,
				name,
				description,
				permissions);
	}
}
