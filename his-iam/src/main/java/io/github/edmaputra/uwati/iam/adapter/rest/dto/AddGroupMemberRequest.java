package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.application.port.in.AddGroupMemberCommand;
import io.github.edmaputra.uwati.iam.domain.model.GroupId;
import io.github.edmaputra.uwati.iam.domain.model.UserId;

/**
 * REST request payload for adding a user to a group.
 *
 * @param userId the user ID string to add
 * @author edmaputra
 */
public record AddGroupMemberRequest(String userId) {

	/**
	 * Converts this request DTO into a domain {@link AddGroupMemberCommand}.
	 *
	 * @param tenantId the tenant ID
	 * @param groupId  the group ID
	 * @return the command record
	 */
	public AddGroupMemberCommand toCommand(TenantId tenantId, GroupId groupId) {
		return new AddGroupMemberCommand(tenantId, groupId, UserId.from(userId));
	}
}
