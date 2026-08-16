package io.github.edmaputra.uwati.core.tenancy.application;

import java.lang.ScopedValue;

import io.github.edmaputra.uwati.core.tenancy.domain.TenantId;

public interface TenantContextScope extends TenantContext {

	<T, X extends Throwable> T callWithTenant(TenantId tenantId, ScopedValue.CallableOp<T, X> operation) throws X;
}
