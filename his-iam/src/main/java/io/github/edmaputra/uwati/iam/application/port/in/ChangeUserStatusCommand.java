package io.github.edmaputra.uwati.iam.application.port.in;

import java.util.Objects;

import io.github.edmaputra.uwati.iam.domain.model.UserId;
import io.github.edmaputra.uwati.iam.domain.model.UserStatus;

/**
 * Command for changing user lifecycle status.
 *
 * @param userId the user ID
 * @param status the target user status
 * @author edmaputra
 */
public record ChangeUserStatusCommand(
		UserId userId,
		UserStatus status) {

	public ChangeUserStatusCommand {
		Objects.requireNonNull(userId, "UserId must not be null.");
		Objects.requireNonNull(status, "Status must not be null.");
	}
}
