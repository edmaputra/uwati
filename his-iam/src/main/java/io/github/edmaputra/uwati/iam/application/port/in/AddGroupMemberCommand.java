package io.github.edmaputra.uwati.iam.application.port.in;

import java.util.Objects;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.domain.model.GroupId;
import io.github.edmaputra.uwati.iam.domain.model.UserId;

/**
 * Command for adding a user to a group.
 *
 * @param tenantId the owning tenant ID
 * @param groupId  the group ID
 * @param userId   the user ID to add
 * @author edmaputra
 */
public record AddGroupMemberCommand(
		TenantId tenantId,
		GroupId groupId,
		UserId userId) {

	public AddGroupMemberCommand {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		Objects.requireNonNull(groupId, "GroupId must not be null.");
		Objects.requireNonNull(userId, "UserId must not be null.");
	}
}
