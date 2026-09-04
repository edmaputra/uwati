package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import java.time.Instant;
import java.util.List;

import io.github.edmaputra.uwati.iam.domain.model.User;
import io.github.edmaputra.uwati.iam.domain.model.UserIdentity;
import io.github.edmaputra.uwati.iam.domain.model.UserRoleAssignment;

/**
 * REST response representing a detailed user account profile with direct role assignments and linked identities.
 *
 * @param id                the user ID
 * @param email             the email address
 * @param fullName          the user's full name
 * @param status            the user status
 * @param isSuperAdmin      whether the user is a platform superadmin
 * @param directAssignments list of direct role assignments
 * @param identities        list of linked federated identities
 * @param createdAt         creation timestamp
 * @param updatedAt         last updated timestamp
 * @author edmaputra
 */
public record UserDetailResponse(
		String id,
		String email,
		String fullName,
		String status,
		boolean isSuperAdmin,
		List<UserRoleAssignmentResponse> directAssignments,
		List<UserIdentityResponse> identities,
		Instant createdAt,
		Instant updatedAt) {

	/**
	 * Factory method composing a detailed response DTO.
	 *
	 * @param user        the user aggregate
	 * @param assignments direct role assignments
	 * @param identities  linked external identities
	 * @return the response DTO
	 */
	public static UserDetailResponse of(
			User user,
			List<UserRoleAssignment> assignments,
			List<UserIdentity> identities) {
		return new UserDetailResponse(
				user.getId().value().toString(),
				user.getEmail(),
				user.getFullName(),
				user.getStatus().name(),
				user.isPlatformSuperAdmin(),
				assignments.stream().map(UserRoleAssignmentResponse::from).toList(),
				identities.stream().map(UserIdentityResponse::from).toList(),
				user.getCreatedAt(),
				user.getUpdatedAt());
	}
}
