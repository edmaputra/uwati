package io.github.edmaputra.uwati.iam.application.port.in;

import java.util.Objects;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;

public record CreateScopeNodeCommand(
		TenantId tenantId,
		ScopeNodeId parentId,
		String code,
		String name) {

	public CreateScopeNodeCommand {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		Objects.requireNonNull(code, "Scope node code must not be null.");
		Objects.requireNonNull(name, "Scope node name must not be null.");
		if (code.isBlank()) {
			throw new IllegalArgumentException("Scope node code must not be blank.");
		}
		if (name.isBlank()) {
			throw new IllegalArgumentException("Scope node name must not be blank.");
		}
	}

	public static CreateScopeNodeCommand root(TenantId tenantId, String code, String name) {
		return new CreateScopeNodeCommand(tenantId, null, code, name);
	}

	public static CreateScopeNodeCommand child(TenantId tenantId, ScopeNodeId parentId, String code, String name) {
		return new CreateScopeNodeCommand(
				tenantId,
				Objects.requireNonNull(parentId, "ParentId must not be null for child nodes."),
				code,
				name);
	}

	public boolean isRoot() {
		return parentId == null;
	}
}
