package io.github.edmaputra.uwati.iam.application.port.in;

import java.util.Objects;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.domain.model.GroupId;

/**
 * Command for updating an existing user group's metadata and SSO mappings.
 *
 * @param tenantId             the owning tenant ID
 * @param groupId              the group ID to update
 * @param name                 the updated name
 * @param description          the updated description
 * @param externalIdpGroupName the updated external IdP group claim mapping
 * @author edmaputra
 */
public record UpdateGroupCommand(
		TenantId tenantId,
		GroupId groupId,
		String name,
		String description,
		String externalIdpGroupName) {

	public UpdateGroupCommand {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		Objects.requireNonNull(groupId, "GroupId must not be null.");
		Objects.requireNonNull(name, "Name must not be null.");
		if (name.isBlank()) {
			throw new IllegalArgumentException("Group name must not be blank.");
		}
	}
}
