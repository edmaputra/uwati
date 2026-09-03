package io.github.edmaputra.uwati.iam.application.port.in;

import java.util.Objects;
import java.util.Set;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.domain.model.RoleId;

/**
 * Command for updating an existing role's name, description, and permissions.
 *
 * @param tenantId    optional tenant ID context
 * @param roleId      the role ID to update
 * @param name        the updated name
 * @param description the updated description
 * @param permissions the updated set of permission codes
 * @author edmaputra
 */
public record UpdateRoleCommand(
		TenantId tenantId,
		RoleId roleId,
		String name,
		String description,
		Set<String> permissions) {

	public UpdateRoleCommand {
		Objects.requireNonNull(roleId, "RoleId must not be null.");
		Objects.requireNonNull(name, "Name must not be null.");
		if (name.isBlank()) {
			throw new IllegalArgumentException("Role name must not be blank.");
		}
		permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
	}
}
