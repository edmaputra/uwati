package io.github.edmaputra.uwati.iam.domain.model;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.util.UuidV7;

/**
 * Strongly-typed value object representing a unique User identifier (UUIDv7).
 *
 * @param value the underlying UUID value
 *
 * @author edmaputra
 */
public record UserId(UUID value) {

	public UserId {
		Objects.requireNonNull(value, "User ID must not be null.");
	}

	/**
	 * Generates a new time-ordered UUIDv7 User identifier.
	 *
	 * @return new {@link UserId}
	 */
	public static UserId generate() {
		return new UserId(UuidV7.generate());
	}

	/**
	 * Creates a UserId from an existing UUID.
	 *
	 * @param value the UUID
	 * @return new {@link UserId}
	 */
	public static UserId of(UUID value) {
		return new UserId(value);
	}

	/**
	 * Creates a UserId from a UUID string representation.
	 *
	 * @param value the UUID string
	 * @return new {@link UserId}
	 */
	public static UserId from(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("User ID must not be blank.");
		}
		return new UserId(UUID.fromString(value.trim()));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}

