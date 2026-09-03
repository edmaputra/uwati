package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import java.time.Instant;
import java.util.Set;

import io.github.edmaputra.uwati.iam.domain.model.Role;

/**
 * REST response representing a role catalog definition.
 *
 * @param id           the unique role ID
 * @param tenantId     the tenant ID (null for global roles)
 * @param code         the unique role code
 * @param name         the role name
 * @param description  optional role description
 * @param isSystemRole whether this role is an immutable system role
 * @param permissions  set of assigned permission keys
 * @param createdAt    creation timestamp
 * @param updatedAt    last updated timestamp
 * @author edmaputra
 */
public record RoleResponse(
		String id,
		String tenantId,
		String code,
		String name,
		String description,
		boolean isSystemRole,
		Set<String> permissions,
		Instant createdAt,
		Instant updatedAt) {

	/**
	 * Maps a domain {@link Role} to a REST response DTO.
	 *
	 * @param role the domain role
	 * @return the response DTO
	 */
	public static RoleResponse from(Role role) {
		return new RoleResponse(
				role.getId().value().toString(),
				role.optionalTenantId().map(t -> t.value().toString()).orElse(null),
				role.getCode(),
				role.getName(),
				role.optionalDescription().orElse(null),
				role.isSystemRole(),
				role.permissions(),
				role.getCreatedAt(),
				role.getUpdatedAt());
	}
}
