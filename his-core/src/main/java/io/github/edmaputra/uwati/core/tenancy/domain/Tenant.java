package io.github.edmaputra.uwati.core.tenancy.domain;

import java.util.Objects;

public record Tenant(TenantId id, String name, TenantStatus status) {

	public Tenant {
		Objects.requireNonNull(id, "Tenant ID must not be null.");
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Tenant name must not be blank.");
		}
		Objects.requireNonNull(status, "Tenant status must not be null.");
	}

	public boolean isActive() {
		return status == TenantStatus.ACTIVE;
	}
}
