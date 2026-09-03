package io.github.edmaputra.uwati.iam.application.port.in;

import java.util.Objects;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.domain.model.RoleId;

/**
 * Command for deleting a custom role.
 *
 * @param tenantId optional tenant ID context
 * @param roleId   the role ID to delete
 * @author edmaputra
 */
public record DeleteRoleCommand(
		TenantId tenantId,
		RoleId roleId) {

	public DeleteRoleCommand {
		Objects.requireNonNull(roleId, "RoleId must not be null.");
	}
}
