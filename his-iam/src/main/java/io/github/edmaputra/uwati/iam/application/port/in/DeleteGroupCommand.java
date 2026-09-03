package io.github.edmaputra.uwati.iam.application.port.in;

import java.util.Objects;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.domain.model.GroupId;

/**
 * Command for deleting a user group.
 *
 * @param tenantId the owning tenant ID
 * @param groupId  the group ID to delete
 * @author edmaputra
 */
public record DeleteGroupCommand(
		TenantId tenantId,
		GroupId groupId) {

	public DeleteGroupCommand {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		Objects.requireNonNull(groupId, "GroupId must not be null.");
	}
}
