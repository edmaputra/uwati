package io.github.edmaputra.uwati.iam.application.port.in;

import java.util.Objects;

import io.github.edmaputra.uwati.iam.domain.model.UserId;

/**
 * Command for updating or resetting a user's password.
 *
 * @param userId      the user ID
 * @param newPassword the new raw password
 * @author edmaputra
 */
public record UpdatePasswordCommand(
		UserId userId,
		String newPassword) {

	public UpdatePasswordCommand {
		Objects.requireNonNull(userId, "UserId must not be null.");
		Objects.requireNonNull(newPassword, "New password must not be null.");
		if (newPassword.length() < 6) {
			throw new IllegalArgumentException("Password must be at least 6 characters long.");
		}
	}
}
