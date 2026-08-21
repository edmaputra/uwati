package io.github.edmaputra.uwati.domain.tenancy.domain;

import java.util.Objects;

public record TenantSetting(
		TenantId tenantId,
		String key,
		String value,
		int revision) {

	public TenantSetting {
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		if (key == null || key.isBlank()) {
			throw new IllegalArgumentException("Setting key must not be blank.");
		}
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Setting value must not be blank.");
		}
		if (revision < 1) {
			throw new IllegalArgumentException("Revision must be greater than or equal to 1.");
		}
	}

	public TenantSetting withIncrementedRevision(String newValue) {
		return new TenantSetting(tenantId, key, newValue, revision + 1);
	}
}
