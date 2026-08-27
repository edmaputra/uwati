package io.github.edmaputra.uwati.iam.domain.model;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.util.UuidV7;

public record ScopeNodeId(UUID value) {

	public ScopeNodeId {
		Objects.requireNonNull(value, "ScopeNode ID must not be null.");
	}

	public static ScopeNodeId generate() {
		return new ScopeNodeId(UuidV7.generate());
	}

	public static ScopeNodeId of(UUID value) {
		return new ScopeNodeId(value);
	}

	public static ScopeNodeId from(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("ScopeNode ID must not be blank.");
		}
		return new ScopeNodeId(UUID.fromString(value));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
