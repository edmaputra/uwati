package io.github.edmaputra.uwati.core.tenancy.domain;

import java.util.Objects;
import java.util.UUID;

public record TenantId(UUID value) {

	public TenantId {
		Objects.requireNonNull(value, "Tenant ID must not be null.");
	}

	public static TenantId generate() {
		return new TenantId(UUID.randomUUID());
	}

	public static TenantId from(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Tenant ID must not be blank.");
		}
		return new TenantId(UUID.fromString(value));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
