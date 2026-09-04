package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import java.time.Instant;

import io.github.edmaputra.uwati.iam.domain.model.GroupRoleAssignment;

/**
 * REST response representing a group-level role assignment.
 *
 * @param id              the unique assignment ID
 * @param groupId         the group ID
 * @param roleId          the assigned role ID
 * @param tenantId        the tenant ID
 * @param scopeNodeId     optional scope node ID
 * @param inheritChildren whether permissions cascade downward
 * @param createdAt       creation timestamp
 * @author edmaputra
 */
public record GroupRoleAssignmentResponse(
		String id,
		String groupId,
		String roleId,
		String tenantId,
		String scopeNodeId,
		boolean inheritChildren,
		Instant createdAt) {

	/**
	 * Maps a domain {@link GroupRoleAssignment} to a REST response DTO.
	 *
	 * @param assignment the domain assignment
	 * @return the response DTO
	 */
	public static GroupRoleAssignmentResponse from(GroupRoleAssignment assignment) {
		return new GroupRoleAssignmentResponse(
				assignment.getId().value().toString(),
				assignment.getGroupId().value().toString(),
				assignment.getRoleId().value().toString(),
				assignment.getTenantId().value().toString(),
				assignment.optionalScopeNodeId().map(s -> s.value().toString()).orElse(null),
				assignment.isInheritChildren(),
				assignment.getCreatedAt());
	}
}
