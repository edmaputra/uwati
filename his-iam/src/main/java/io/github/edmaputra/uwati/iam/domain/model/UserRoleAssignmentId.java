package io.github.edmaputra.uwati.iam.domain.model;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.util.UuidV7;

public record UserRoleAssignmentId(UUID value) {

	public UserRoleAssignmentId {
		Objects.requireNonNull(value, "UserRoleAssignment ID must not be null.");
	}

	public static UserRoleAssignmentId generate() {
		return new UserRoleAssignmentId(UuidV7.generate());
	}

	public static UserRoleAssignmentId of(UUID value) {
		return new UserRoleAssignmentId(value);
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
