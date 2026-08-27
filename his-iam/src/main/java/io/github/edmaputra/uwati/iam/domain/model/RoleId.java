package io.github.edmaputra.uwati.iam.domain.model;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.util.UuidV7;

public record RoleId(UUID value) {

	public RoleId {
		Objects.requireNonNull(value, "Role ID must not be null.");
	}

	public static RoleId generate() {
		return new RoleId(UuidV7.generate());
	}

	public static RoleId of(UUID value) {
		return new RoleId(value);
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
