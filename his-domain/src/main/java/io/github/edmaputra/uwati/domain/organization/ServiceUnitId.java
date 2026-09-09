package io.github.edmaputra.uwati.domain.organization;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.util.UuidV7;

public record ServiceUnitId(UUID value) {

	public ServiceUnitId {
		Objects.requireNonNull(value, "Service Unit ID must not be null.");
	}

	public static ServiceUnitId generate() {
		return new ServiceUnitId(UuidV7.generate());
	}

	public static ServiceUnitId from(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Service Unit ID must not be blank.");
		}
		return new ServiceUnitId(UUID.fromString(value));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
