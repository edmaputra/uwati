package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.application.port.in.UpdateGroupCommand;
import io.github.edmaputra.uwati.iam.domain.model.GroupId;

/**
 * REST request payload for updating a user group.
 *
 * @param name                 the updated name
 * @param description          the updated description
 * @param externalIdpGroupName the updated external IdP group claim mapping
 * @author edmaputra
 */
public record UpdateGroupRequest(
		String name,
		String description,
		String externalIdpGroupName) {

	/**
	 * Converts this request DTO into a domain {@link UpdateGroupCommand}.
	 *
	 * @param tenantId the owning tenant ID
	 * @param groupId  the group ID
	 * @return the command record
	 */
	public UpdateGroupCommand toCommand(TenantId tenantId, GroupId groupId) {
		return new UpdateGroupCommand(
				tenantId,
				groupId,
				name,
				description,
				externalIdpGroupName);
	}
}
