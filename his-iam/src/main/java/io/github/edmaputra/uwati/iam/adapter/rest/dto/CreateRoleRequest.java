package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import java.util.Set;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.application.port.in.CreateRoleCommand;

/**
 * REST request payload for creating a custom tenant role.
 *
 * @param code        the uppercase role code
 * @param name        the human-readable name
 * @param description optional description
 * @param permissions set of permission keys to assign
 * @author edmaputra
 */
public record CreateRoleRequest(
		String code,
		String name,
		String description,
		Set<String> permissions) {

	/**
	 * Converts this request DTO into a domain {@link CreateRoleCommand}.
	 *
	 * @param tenantId the owning tenant ID
	 * @return the command record
	 */
	public CreateRoleCommand toCommand(TenantId tenantId) {
		return new CreateRoleCommand(
				tenantId,
				code,
				name,
				description,
				permissions);
	}
}
