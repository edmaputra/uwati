package io.github.edmaputra.uwati.iam.application.port.in;

import java.util.Objects;

import io.github.edmaputra.uwati.iam.domain.model.UserId;

/**
 * Command for updating user profile information.
 *
 * @param userId   the user ID
 * @param fullName the updated full name
 * @author edmaputra
 */
public record UpdateUserProfileCommand(
		UserId userId,
		String fullName) {

	public UpdateUserProfileCommand {
		Objects.requireNonNull(userId, "UserId must not be null.");
		Objects.requireNonNull(fullName, "FullName must not be null.");
		if (fullName.isBlank()) {
			throw new IllegalArgumentException("FullName must not be blank.");
		}
	}
}
