package io.github.edmaputra.uwati.iam.application.port.in;

import java.util.Objects;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;

/**
 * Command for creating a new user group within a tenant.
 *
 * @param tenantId             the owning tenant ID
 * @param code                 the uppercase group code
 * @param name                 the human-readable name
 * @param description          optional description
 * @param externalIdpGroupName optional external IdP group claim mapping
 * @author edmaputra
 */
public record CreateGroupCommand(
		TenantId tenantId,
		String code,
		String name,
		String description,
		String externalIdpGroupName) {

	public CreateGroupCommand {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		Objects.requireNonNull(code, "Code must not be null.");
		if (code.isBlank()) {
			throw new IllegalArgumentException("Group code must not be blank.");
		}
		Objects.requireNonNull(name, "Name must not be null.");
		if (name.isBlank()) {
			throw new IllegalArgumentException("Group name must not be blank.");
		}
	}
}
