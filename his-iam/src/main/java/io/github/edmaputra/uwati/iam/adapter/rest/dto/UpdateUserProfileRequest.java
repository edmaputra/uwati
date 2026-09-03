package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import io.github.edmaputra.uwati.iam.application.port.in.UpdateUserProfileCommand;
import io.github.edmaputra.uwati.iam.domain.model.UserId;

/**
 * REST request payload for updating a user's full name.
 *
 * @param fullName the updated full name
 * @author edmaputra
 */
public record UpdateUserProfileRequest(String fullName) {

	/**
	 * Converts this request DTO into a domain {@link UpdateUserProfileCommand}.
	 *
	 * @param userId the target user ID
	 * @return the command record
	 */
	public UpdateUserProfileCommand toCommand(UserId userId) {
		return new UpdateUserProfileCommand(userId, fullName);
	}
}
