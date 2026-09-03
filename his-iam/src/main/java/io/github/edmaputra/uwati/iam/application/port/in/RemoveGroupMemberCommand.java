package io.github.edmaputra.uwati.iam.application.port.in;

import java.util.Objects;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.domain.model.GroupId;
import io.github.edmaputra.uwati.iam.domain.model.UserId;

/**
 * Command for removing a user from a group.
 *
 * @param tenantId the owning tenant ID
 * @param groupId  the group ID
 * @param userId   the user ID to remove
 * @author edmaputra
 */
public record RemoveGroupMemberCommand(
		TenantId tenantId,
		GroupId groupId,
		UserId userId) {

	public RemoveGroupMemberCommand {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		Objects.requireNonNull(groupId, "GroupId must not be null.");
		Objects.requireNonNull(userId, "UserId must not be null.");
	}
}
