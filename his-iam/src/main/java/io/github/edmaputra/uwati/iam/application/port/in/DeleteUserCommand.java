package io.github.edmaputra.uwati.iam.application.port.in;

import java.util.Objects;

import io.github.edmaputra.uwati.iam.domain.model.UserId;

/**
 * Command for deactivating or deleting a user.
 *
 * @param userId the user ID
 * @author edmaputra
 */
public record DeleteUserCommand(UserId userId) {

	public DeleteUserCommand {
		Objects.requireNonNull(userId, "UserId must not be null.");
	}
}
