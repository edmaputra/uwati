package io.github.edmaputra.uwati.iam.domain.model;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.util.UuidV7;

public record UserId(UUID value) {

	public UserId {
		Objects.requireNonNull(value, "User ID must not be null.");
	}

	public static UserId generate() {
		return new UserId(UuidV7.generate());
	}

	public static UserId from(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("User ID must not be blank.");
		}
		return new UserId(UUID.fromString(value));
	}

	public static UserId of(UUID value) {
		return new UserId(value);
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
