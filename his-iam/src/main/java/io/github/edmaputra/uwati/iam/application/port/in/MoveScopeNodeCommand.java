package io.github.edmaputra.uwati.iam.application.port.in;

import java.util.Objects;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;

public record MoveScopeNodeCommand(
		TenantId tenantId,
		ScopeNodeId id,
		ScopeNodeId newParentId) {

	public MoveScopeNodeCommand {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		Objects.requireNonNull(id, "Target ScopeNodeId must not be null.");
		Objects.requireNonNull(newParentId, "New parent ScopeNodeId must not be null.");
		if (id.equals(newParentId)) {
			throw new IllegalArgumentException("Cannot move scope node to itself.");
		}
	}
}
