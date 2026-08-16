package io.github.edmaputra.uwati.bootstrap.tenancy;

import java.util.Optional;

import org.springframework.stereotype.Component;

import io.github.edmaputra.uwati.core.tenancy.application.TenantContextScope;
import io.github.edmaputra.uwati.core.tenancy.domain.TenantId;

@Component
public class ThreadLocalTenantContext implements TenantContextScope {

	private final ThreadLocal<TenantId> tenantId = new ThreadLocal<>();

	@Override
	public Optional<TenantId> currentTenantId() {
		return Optional.ofNullable(tenantId.get());
	}

	@Override
	public Scope open(TenantId tenantId) {
		TenantId previousTenantId = this.tenantId.get();
		this.tenantId.set(tenantId);
		return () -> {
			if (previousTenantId == null) {
				this.tenantId.remove();
			}
			else {
				this.tenantId.set(previousTenantId);
			}
		};
	}
}
