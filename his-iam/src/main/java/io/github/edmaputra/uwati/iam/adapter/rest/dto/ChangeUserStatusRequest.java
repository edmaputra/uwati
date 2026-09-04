package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import io.github.edmaputra.uwati.iam.application.port.in.ChangeUserStatusCommand;
import io.github.edmaputra.uwati.iam.domain.model.UserId;
import io.github.edmaputra.uwati.iam.domain.model.UserStatus;

/**
 * REST request payload for changing user lifecycle status.
 *
 * @param status the target status string (ACTIVE, SUSPENDED, DEACTIVATED)
 * @author edmaputra
 */
public record ChangeUserStatusRequest(String status) {

	/**
	 * Converts this request DTO into a domain {@link ChangeUserStatusCommand}.
	 *
	 * @param userId the target user ID
	 * @return the command record
	 */
	public ChangeUserStatusCommand toCommand(UserId userId) {
		if (status == null || status.isBlank()) {
			throw new IllegalArgumentException("Status must not be blank.");
		}
		return new ChangeUserStatusCommand(userId, UserStatus.valueOf(status.trim().toUpperCase()));
	}
}
