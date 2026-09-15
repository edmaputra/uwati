package io.github.edmaputra.uwati.bootstrap.tenancy;

import java.lang.ScopedValue;
import java.util.Objects;
import java.util.Optional;

import java.util.UUID;

import org.springframework.stereotype.Component;

import io.github.edmaputra.uwati.domain.tenancy.application.TenantContextScope;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.iam.domain.tenancy.TenantContextBridge;

/**
 * Thread-bound tenant context implementation using Java {@link ScopedValue}.
 * <p>
 * Implements both {@link TenantContextScope} and IAM's {@link TenantContextBridge} to provide
 * lightweight, virtual-thread-friendly contextual tenant propagation across operations.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@Component
public class ScopedValueTenantContext implements TenantContextScope, TenantContextBridge {

	private static final ScopedValue<TenantId> TENANT_ID = ScopedValue.newInstance();

	/**
	 * Returns the currently bound tenant identifier, if available.
	 *
	 * @return an {@link Optional} containing the currently bound {@link TenantId}, or empty if unbounded
	 */
	@Override
	public Optional<TenantId> currentTenantId() {
		return TENANT_ID.isBound() ? Optional.of(TENANT_ID.get()) : Optional.empty();
	}

	/**
	 * Executes a callable operation within the context of a bound tenant.
	 *
	 * @param <T> the result type returned by the operation
	 * @param <X> the type of exception that may be thrown by the operation
	 * @param tenantId the tenant ID to bind during execution
	 * @param operation the callable operation to execute within the tenant scope
	 * @return the result of the callable operation
	 * @throws NullPointerException if {@code tenantId} or {@code operation} is null
	 * @throws X if the operation throws an exception
	 */
	@Override
	public <T, X extends Throwable> T callWithTenant(TenantId tenantId, ScopedValue.CallableOp<T, X> operation)
			throws X {
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		Objects.requireNonNull(operation, "Tenant operation must not be null.");
		return ScopedValue.where(TENANT_ID, tenantId).call(operation);
	}

	/**
	 * Executes a throwing runnable within the context of a bound tenant UUID.
	 *
	 * @param <E> the type of exception that may be thrown by the runnable
	 * @param tenantId the raw tenant UUID to bind
	 * @param runnable the throwing runnable to execute within the tenant scope
	 * @throws NullPointerException if {@code tenantId} or {@code runnable} is null
	 * @throws E if the runnable throws an exception
	 */
	@Override
	public <E extends Throwable> void runWithTenant(UUID tenantId, ThrowingRunnable<E> runnable) throws E {
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		Objects.requireNonNull(runnable, "Runnable must not be null.");
		callWithTenant(new TenantId(tenantId), () -> {
			runnable.run();
			return null;
		});
	}
}
