package io.github.edmaputra.uwati.iam.domain.model;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.util.UuidV7;

/**
 * Strongly-typed value object representing a unique UserIdentity identifier (UUIDv7).
 *
 * @param value the underlying UUID value
 *
 * @author edmaputra
 */
public record UserIdentityId(UUID value) {

	public UserIdentityId {
		Objects.requireNonNull(value, "UserIdentityId value must not be null.");
	}

	/**
	 * Generates a new time-ordered UUIDv7 UserIdentity identifier.
	 *
	 * @return new {@link UserIdentityId}
	 */
	public static UserIdentityId generate() {
		return new UserIdentityId(UuidV7.generate());
	}

	/**
	 * Creates a UserIdentityId from an existing UUID.
	 *
	 * @param value the UUID
	 * @return new {@link UserIdentityId}
	 */
	public static UserIdentityId of(UUID value) {
		return new UserIdentityId(value);
	}

	/**
	 * Creates a UserIdentityId from a UUID string representation.
	 *
	 * @param value the UUID string
	 * @return new {@link UserIdentityId}
	 */
	public static UserIdentityId from(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("UserIdentity ID must not be blank.");
		}
		return new UserIdentityId(UUID.fromString(value.trim()));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}

