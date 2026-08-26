package io.github.edmaputra.uwati.domain.tenancy.domain;

import java.util.Map;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.audit.Auditable;

public record TenantSetting(
		TenantId tenantId,
		String key,
		String value,
		int revision) implements Auditable {

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

	@Override
	public Map<String, Object> auditableFields() {
		return Map.of(
				"value", value,
				"revision", revision);
	}
}
