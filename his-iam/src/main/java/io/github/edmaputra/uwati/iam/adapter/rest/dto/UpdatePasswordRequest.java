package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import io.github.edmaputra.uwati.iam.application.port.in.UpdatePasswordCommand;
import io.github.edmaputra.uwati.iam.domain.model.UserId;

/**
 * REST request payload for updating or resetting a user password.
 *
 * @param newPassword the new raw password
 * @author edmaputra
 */
public record UpdatePasswordRequest(String newPassword) {

	/**
	 * Converts this request DTO into a domain {@link UpdatePasswordCommand}.
	 *
	 * @param userId the target user ID
	 * @return the command record
	 */
	public UpdatePasswordCommand toCommand(UserId userId) {
		return new UpdatePasswordCommand(userId, newPassword);
	}
}
