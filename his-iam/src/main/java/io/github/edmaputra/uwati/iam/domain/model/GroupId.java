package io.github.edmaputra.uwati.iam.domain.model;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.util.UuidV7;

/**
 * Strongly-typed value object representing a unique Group identifier (UUIDv7).
 *
 * @param value the underlying UUID value
 *
 * @author edmaputra
 */
public record GroupId(UUID value) {

	public GroupId {
		Objects.requireNonNull(value, "Group ID must not be null.");
	}

	/**
	 * Generates a new time-ordered UUIDv7 Group identifier.
	 *
	 * @return new {@link GroupId}
	 */
	public static GroupId generate() {
		return new GroupId(UuidV7.generate());
	}

	/**
	 * Creates a GroupId from an existing UUID.
	 *
	 * @param value the UUID
	 * @return new {@link GroupId}
	 */
	public static GroupId of(UUID value) {
		return new GroupId(value);
	}

	/**
	 * Creates a GroupId from a UUID string representation.
	 *
	 * @param value the UUID string
	 * @return new {@link GroupId}
	 */
	public static GroupId from(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Group ID must not be blank.");
		}
		return new GroupId(UUID.fromString(value.trim()));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}

