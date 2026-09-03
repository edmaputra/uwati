package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import java.time.Instant;

import io.github.edmaputra.uwati.iam.domain.model.Group;

/**
 * REST response representing a user group or team.
 *
 * @param id                   the unique group ID
 * @param tenantId             the owning tenant ID
 * @param code                 the uppercase group code
 * @param name                 the human-readable group name
 * @param description          optional description
 * @param externalIdpGroupName optional external IdP group claim mapping
 * @param createdAt            creation timestamp
 * @param updatedAt            last updated timestamp
 * @author edmaputra
 */
public record GroupResponse(
		String id,
		String tenantId,
		String code,
		String name,
		String description,
		String externalIdpGroupName,
		Instant createdAt,
		Instant updatedAt) {

	/**
	 * Maps a domain {@link Group} to a REST response DTO.
	 *
	 * @param group the domain group
	 * @return the response DTO
	 */
	public static GroupResponse from(Group group) {
		return new GroupResponse(
				group.getId().value().toString(),
				group.getTenantId().value().toString(),
				group.getCode(),
				group.getName(),
				group.optionalDescription().orElse(null),
				group.optionalExternalIdpGroupName().orElse(null),
				group.getCreatedAt(),
				group.getUpdatedAt());
	}
}
