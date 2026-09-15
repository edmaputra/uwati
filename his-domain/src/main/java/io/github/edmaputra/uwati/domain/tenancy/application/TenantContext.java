package io.github.edmaputra.uwati.domain.tenancy.application;

import java.util.Optional;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;

/**
 * Inbound application context interface providing access to the currently active tenant.
 * <p>
 * Exposes methods to retrieve or require tenant identity for thread-local or scoped execution
 * within the application layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public interface TenantContext {

	/**
	 * Retrieves the current tenant identifier, if one is active.
	 *
	 * @return an {@link Optional} containing the active {@link TenantId}, or empty if none is bound
	 */
	Optional<TenantId> currentTenantId();

	/**
	 * Requires that a tenant context is present and returns its identifier.
	 *
	 * @return the active {@link TenantId}
	 * @throws MissingTenantContextException if no tenant context is bound to the current execution
	 */
	default TenantId requireTenantId() {
		return currentTenantId().orElseThrow(
				() -> new MissingTenantContextException("A tenant context is required for this operation."));
	}
}
