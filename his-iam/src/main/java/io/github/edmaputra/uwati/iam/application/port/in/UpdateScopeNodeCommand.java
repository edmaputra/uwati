package io.github.edmaputra.uwati.iam.application.port.in;

import java.util.Objects;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;

public record UpdateScopeNodeCommand(
		TenantId tenantId,
		ScopeNodeId id,
		String code,
		String name) {

	public UpdateScopeNodeCommand {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		Objects.requireNonNull(id, "ScopeNodeId must not be null.");
		Objects.requireNonNull(code, "Scope node code must not be null.");
		Objects.requireNonNull(name, "Scope node name must not be null.");
		if (code.isBlank()) {
			throw new IllegalArgumentException("Scope node code must not be blank.");
		}
		if (name.isBlank()) {
			throw new IllegalArgumentException("Scope node name must not be blank.");
		}
	}
}
