package io.github.edmaputra.uwati.domain.tenancy.domain;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.util.UuidV7;

public record TenantId(UUID value) {

	public TenantId {
		Objects.requireNonNull(value, "Tenant ID must not be null.");
	}

	public static TenantId generate() {
		return new TenantId(UuidV7.generate());
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
