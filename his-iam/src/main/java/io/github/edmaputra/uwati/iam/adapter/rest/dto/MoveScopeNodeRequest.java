package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.application.port.in.MoveScopeNodeCommand;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;

/**
 * REST request payload for moving/re-parenting a scope node.
 *
 * @param newParentId the target parent scope node ID string (null or blank to move to root)
 * @author edmaputra
 */
public record MoveScopeNodeRequest(String newParentId) {

	/**
	 * Converts this request DTO into a domain {@link MoveScopeNodeCommand}.
	 *
	 * @param tenantId the owning tenant ID
	 * @param id       the scope node ID to move
	 * @return the command record
	 */
	public MoveScopeNodeCommand toCommand(TenantId tenantId, ScopeNodeId id) {
		return new MoveScopeNodeCommand(
				tenantId,
				id,
				newParentId != null && !newParentId.isBlank() ? ScopeNodeId.from(newParentId) : null);
	}
}
