package io.github.edmaputra.uwati.core.tenancy.application;

import java.util.Optional;

import io.github.edmaputra.uwati.core.tenancy.domain.TenantId;

public interface TenantContext {

	Optional<TenantId> currentTenantId();

	default TenantId requireTenantId() {
		return currentTenantId().orElseThrow(
				() -> new MissingTenantContextException("A tenant context is required for this operation."));
	}
}
