package io.github.edmaputra.uwati.iam.application.port.in;

import java.util.Objects;
import java.util.Set;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;

/**
 * Command for creating a new custom role within a tenant.
 *
 * @param tenantId    the owning tenant ID (null for global roles)
 * @param code        the uppercase unique role code
 * @param name        the human-readable role name
 * @param description optional role description
 * @param permissions the initial set of permission codes
 * @author edmaputra
 */
public record CreateRoleCommand(
		TenantId tenantId,
		String code,
		String name,
		String description,
		Set<String> permissions) {

	public CreateRoleCommand {
		Objects.requireNonNull(code, "Role code must not be null.");
		if (code.isBlank()) {
			throw new IllegalArgumentException("Role code must not be blank.");
		}
		Objects.requireNonNull(name, "Role name must not be null.");
		if (name.isBlank()) {
			throw new IllegalArgumentException("Role name must not be blank.");
		}
		permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
	}
}
