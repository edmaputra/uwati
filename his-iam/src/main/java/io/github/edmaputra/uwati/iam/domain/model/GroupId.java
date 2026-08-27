package io.github.edmaputra.uwati.iam.domain.model;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.util.UuidV7;

public record GroupId(UUID value) {

	public GroupId {
		Objects.requireNonNull(value, "Group ID must not be null.");
	}

	public static GroupId generate() {
		return new GroupId(UuidV7.generate());
	}

	public static GroupId of(UUID value) {
		return new GroupId(value);
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
