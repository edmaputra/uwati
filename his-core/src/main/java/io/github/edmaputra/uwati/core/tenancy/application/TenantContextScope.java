package io.github.edmaputra.uwati.core.tenancy.application;

import io.github.edmaputra.uwati.core.tenancy.domain.TenantId;

public interface TenantContextScope extends TenantContext {

	Scope open(TenantId tenantId);

	interface Scope extends AutoCloseable {

		@Override
		void close();
	}
}
