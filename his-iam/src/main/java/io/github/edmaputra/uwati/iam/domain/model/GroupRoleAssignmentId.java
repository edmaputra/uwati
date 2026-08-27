package io.github.edmaputra.uwati.iam.domain.model;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.util.UuidV7;

public record GroupRoleAssignmentId(UUID value) {

	public GroupRoleAssignmentId {
		Objects.requireNonNull(value, "GroupRoleAssignment ID must not be null.");
	}

	public static GroupRoleAssignmentId generate() {
		return new GroupRoleAssignmentId(UuidV7.generate());
	}

	public static GroupRoleAssignmentId of(UUID value) {
		return new GroupRoleAssignmentId(value);
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
