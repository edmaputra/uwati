package io.github.edmaputra.uwati.iam.domain.model;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.util.UuidV7;

/**
 * Strongly-typed value object representing a unique GroupRoleAssignment identifier (UUIDv7).
 *
 * @param value the underlying UUID value
 *
 * @author edmaputra
 */
public record GroupRoleAssignmentId(UUID value) {

	public GroupRoleAssignmentId {
		Objects.requireNonNull(value, "GroupRoleAssignmentId value must not be null.");
	}

	/**
	 * Generates a new time-ordered UUIDv7 GroupRoleAssignment identifier.
	 *
	 * @return new {@link GroupRoleAssignmentId}
	 */
	public static GroupRoleAssignmentId generate() {
		return new GroupRoleAssignmentId(UuidV7.generate());
	}

	/**
	 * Creates a GroupRoleAssignmentId from an existing UUID.
	 *
	 * @param value the UUID
	 * @return new {@link GroupRoleAssignmentId}
	 */
	public static GroupRoleAssignmentId of(UUID value) {
		return new GroupRoleAssignmentId(value);
	}

	/**
	 * Creates a GroupRoleAssignmentId from a UUID string representation.
	 *
	 * @param value the UUID string
	 * @return new {@link GroupRoleAssignmentId}
	 */
	public static GroupRoleAssignmentId from(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("GroupRoleAssignment ID must not be blank.");
		}
		return new GroupRoleAssignmentId(UUID.fromString(value.trim()));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}

