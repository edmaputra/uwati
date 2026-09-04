package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import java.time.Instant;

import io.github.edmaputra.uwati.iam.domain.model.UserRoleAssignment;

/**
 * REST response representing a direct role assignment binding to a user.
 *
 * @param id              the unique assignment ID
 * @param userId          the assigned user ID
 * @param roleId          the assigned role ID
 * @param tenantId        the tenant ID context
 * @param scopeNodeId     optional scope node ID
 * @param inheritChildren whether permissions cascade downward
 * @param createdAt       creation timestamp
 * @author edmaputra
 */
public record UserRoleAssignmentResponse(
		String id,
		String userId,
		String roleId,
		String tenantId,
		String scopeNodeId,
		boolean inheritChildren,
		Instant createdAt) {

	/**
	 * Maps a domain {@link UserRoleAssignment} to a REST response DTO.
	 *
	 * @param assignment the domain assignment
	 * @return the response DTO
	 */
	public static UserRoleAssignmentResponse from(UserRoleAssignment assignment) {
		return new UserRoleAssignmentResponse(
				assignment.getId().value().toString(),
				assignment.getUserId().value().toString(),
				assignment.getRoleId().value().toString(),
				assignment.optionalTenantId().map(t -> t.value().toString()).orElse(null),
				assignment.optionalScopeNodeId().map(s -> s.value().toString()).orElse(null),
				assignment.isInheritChildren(),
				assignment.getCreatedAt());
	}
}
