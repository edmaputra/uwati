package io.github.edmaputra.uwati.iam.domain.model;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.util.UuidV7;

public record UserIdentityId(UUID value) {

	public UserIdentityId {
		Objects.requireNonNull(value, "UserIdentity ID must not be null.");
	}

	public static UserIdentityId generate() {
		return new UserIdentityId(UuidV7.generate());
	}

	public static UserIdentityId of(UUID value) {
		return new UserIdentityId(value);
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
