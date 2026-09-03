package io.github.edmaputra.uwati.iam.application.port.in;

import io.github.edmaputra.uwati.iam.domain.model.UserStatus;

/**
 * Query criteria for searching and filtering users.
 *
 * @param search optional keyword filter matching email or full name
 * @param status optional user status filter
 * @author edmaputra
 */
public record UserQuery(
		String search,
		UserStatus status) {

	/**
	 * Creates an empty query matching all users.
	 *
	 * @return empty {@link UserQuery}
	 */
	public static UserQuery all() {
		return new UserQuery(null, null);
	}
}
