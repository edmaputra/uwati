package io.github.edmaputra.uwati.iam.domain.model;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.util.UuidV7;

/**
 * Strongly-typed value object representing a unique Role identifier (UUIDv7).
 *
 * @param value the underlying UUID value
 *
 * @author edmaputra
 */
public record RoleId(UUID value) {

	public RoleId {
		Objects.requireNonNull(value, "Role ID must not be null.");
	}

	/**
	 * Generates a new time-ordered UUIDv7 Role identifier.
	 *
	 * @return new {@link RoleId}
	 */
	public static RoleId generate() {
		return new RoleId(UuidV7.generate());
	}

	/**
	 * Creates a RoleId from an existing UUID.
	 *
	 * @param value the UUID
	 * @return new {@link RoleId}
	 */
	public static RoleId of(UUID value) {
		return new RoleId(value);
	}

	/**
	 * Creates a RoleId from a UUID string representation.
	 *
	 * @param value the UUID string
	 * @return new {@link RoleId}
	 */
	public static RoleId from(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Role ID must not be blank.");
		}
		return new RoleId(UUID.fromString(value.trim()));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}

