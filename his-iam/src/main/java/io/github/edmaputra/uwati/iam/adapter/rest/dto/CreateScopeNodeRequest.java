package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.application.port.in.CreateScopeNodeCommand;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;

/**
 * REST request payload for creating a scope node.
 *
 * @param code     the unique node code
 * @param name     the node name
 * @param parentId optional parent node ID string
 * @author edmaputra
 */
public record CreateScopeNodeRequest(
		String code,
		String name,
		String parentId) {

	/**
	 * Converts this request DTO into a domain {@link CreateScopeNodeCommand}.
	 *
	 * @param tenantId the owning tenant ID
	 * @return the command record
	 */
	public CreateScopeNodeCommand toCommand(TenantId tenantId) {
		return new CreateScopeNodeCommand(
				tenantId,
				parentId != null && !parentId.isBlank() ? ScopeNodeId.from(parentId) : null,
				code,
				name);
	}
}
