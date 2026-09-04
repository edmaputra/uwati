package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.application.port.in.UpdateScopeNodeCommand;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;

/**
 * REST request payload for updating a scope node's metadata.
 *
 * @param code the updated node code
 * @param name the updated name
 * @author edmaputra
 */
public record UpdateScopeNodeRequest(
		String code,
		String name) {

	/**
	 * Converts this request DTO into a domain {@link UpdateScopeNodeCommand}.
	 *
	 * @param tenantId the owning tenant ID
	 * @param id       the scope node ID
	 * @return the command record
	 */
	public UpdateScopeNodeCommand toCommand(TenantId tenantId, ScopeNodeId id) {
		return new UpdateScopeNodeCommand(tenantId, id, code, name);
	}
}
