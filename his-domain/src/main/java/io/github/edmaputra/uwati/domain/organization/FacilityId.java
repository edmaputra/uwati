package io.github.edmaputra.uwati.domain.organization;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.util.UuidV7;

public record FacilityId(UUID value) {

	public FacilityId {
		Objects.requireNonNull(value, "Facility ID must not be null.");
	}

	public static FacilityId generate() {
		return new FacilityId(UuidV7.generate());
	}

	public static FacilityId from(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Facility ID must not be blank.");
		}
		return new FacilityId(UUID.fromString(value));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
