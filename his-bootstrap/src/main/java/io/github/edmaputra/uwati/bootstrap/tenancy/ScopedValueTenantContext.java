package io.github.edmaputra.uwati.bootstrap.tenancy;

import java.lang.ScopedValue;
import java.util.Objects;
import java.util.Optional;

import java.util.UUID;

import org.springframework.stereotype.Component;

import io.github.edmaputra.uwati.domain.tenancy.application.TenantContextScope;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.iam.domain.tenancy.TenantContextBridge;

@Component
public class ScopedValueTenantContext implements TenantContextScope, TenantContextBridge {

	private static final ScopedValue<TenantId> TENANT_ID = ScopedValue.newInstance();

	@Override
	public Optional<TenantId> currentTenantId() {
		return TENANT_ID.isBound() ? Optional.of(TENANT_ID.get()) : Optional.empty();
	}

	@Override
	public <T, X extends Throwable> T callWithTenant(TenantId tenantId, ScopedValue.CallableOp<T, X> operation)
			throws X {
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		Objects.requireNonNull(operation, "Tenant operation must not be null.");
		return ScopedValue.where(TENANT_ID, tenantId).call(operation);
	}

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
