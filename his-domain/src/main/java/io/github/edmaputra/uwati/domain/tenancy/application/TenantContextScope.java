package io.github.edmaputra.uwati.domain.tenancy.application;

import java.lang.ScopedValue;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;

public interface TenantContextScope extends TenantContext {

	<T, X extends Throwable> T callWithTenant(TenantId tenantId, ScopedValue.CallableOp<T, X> operation) throws X;
}
