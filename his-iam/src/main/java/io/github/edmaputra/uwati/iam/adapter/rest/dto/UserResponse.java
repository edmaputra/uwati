package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import java.time.Instant;

import io.github.edmaputra.uwati.iam.domain.model.User;

/**
 * REST response representing a summary user account profile.
 *
 * @param id           the unique user ID
 * @param email        the email address
 * @param fullName     the user's full name
 * @param status       the lifecycle status
 * @param isSuperAdmin whether this user has platform superadmin privileges
 * @param createdAt    creation timestamp
 * @param updatedAt    last updated timestamp
 * @author edmaputra
 */
public record UserResponse(
		String id,
		String email,
		String fullName,
		String status,
		boolean isSuperAdmin,
		Instant createdAt,
		Instant updatedAt) {

	/**
	 * Maps a domain {@link User} entity to a REST {@link UserResponse}.
	 *
	 * @param user the domain user
	 * @return the response DTO
	 */
	public static UserResponse from(User user) {
		return new UserResponse(
				user.getId().value().toString(),
				user.getEmail(),
				user.getFullName(),
				user.getStatus().name(),
				user.isPlatformSuperAdmin(),
				user.getCreatedAt(),
				user.getUpdatedAt());
	}
}
