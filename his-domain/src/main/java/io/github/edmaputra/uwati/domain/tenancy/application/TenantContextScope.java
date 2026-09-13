package io.github.edmaputra.uwati.domain.tenancy.application;

import java.lang.ScopedValue;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;

/**
 * Application interface defining a scoped execution boundary bound to a specific tenant.
 * <p>
 * Uses Java scoped values to safely run operations within the context of a {@link TenantId}
 * in the application layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public interface TenantContextScope extends TenantContext {

	/**
	 * Executes the given callable operation within the scope of the specified tenant identifier.
	 *
	 * @param <T> the result type of the operation
	 * @param <X> the type of exception that the operation may throw
	 * @param tenantId the tenant ID to bind during operation execution
	 * @param operation the operation to execute within the tenant scope
	 * @return the result of the callable operation
	 * @throws X if the operation throws an exception
	 */
	<T, X extends Throwable> T callWithTenant(TenantId tenantId, ScopedValue.CallableOp<T, X> operation) throws X;
}
