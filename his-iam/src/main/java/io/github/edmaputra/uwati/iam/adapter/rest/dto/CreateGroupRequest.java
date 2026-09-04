package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.application.port.in.CreateGroupCommand;

/**
 * REST request payload for creating a new user group.
 *
 * @param code                 the uppercase group code
 * @param name                 the human-readable name
 * @param description          optional description
 * @param externalIdpGroupName optional external IdP group claim mapping
 * @author edmaputra
 */
public record CreateGroupRequest(
		String code,
		String name,
		String description,
		String externalIdpGroupName) {

	/**
	 * Converts this request DTO into a domain {@link CreateGroupCommand}.
	 *
	 * @param tenantId the owning tenant ID
	 * @return the command record
	 */
	public CreateGroupCommand toCommand(TenantId tenantId) {
		return new CreateGroupCommand(
				tenantId,
				code,
				name,
				description,
				externalIdpGroupName);
	}
}
