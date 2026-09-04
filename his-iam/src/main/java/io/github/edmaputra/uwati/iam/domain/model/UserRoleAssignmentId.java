package io.github.edmaputra.uwati.iam.domain.model;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.util.UuidV7;

/**
 * Strongly-typed value object representing a unique UserRoleAssignment identifier (UUIDv7).
 *
 * @param value the underlying UUID value
 *
 * @author edmaputra
 */
public record UserRoleAssignmentId(UUID value) {

	public UserRoleAssignmentId {
		Objects.requireNonNull(value, "UserRoleAssignmentId value must not be null.");
	}

	/**
	 * Generates a new time-ordered UUIDv7 UserRoleAssignment identifier.
	 *
	 * @return new {@link UserRoleAssignmentId}
	 */
	public static UserRoleAssignmentId generate() {
		return new UserRoleAssignmentId(UuidV7.generate());
	}

	/**
	 * Creates a UserRoleAssignmentId from an existing UUID.
	 *
	 * @param value the UUID
	 * @return new {@link UserRoleAssignmentId}
	 */
	public static UserRoleAssignmentId of(UUID value) {
		return new UserRoleAssignmentId(value);
	}

	/**
	 * Creates a UserRoleAssignmentId from a UUID string representation.
	 *
	 * @param value the UUID string
	 * @return new {@link UserRoleAssignmentId}
	 */
	public static UserRoleAssignmentId from(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("UserRoleAssignment ID must not be blank.");
		}
		return new UserRoleAssignmentId(UUID.fromString(value.trim()));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}

