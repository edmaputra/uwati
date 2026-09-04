package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import java.time.Instant;

import io.github.edmaputra.uwati.iam.domain.model.ScopeNode;

/**
 * REST response representing a scope hierarchy node.
 *
 * @param id        the unique scope node ID
 * @param tenantId  the owning tenant ID
 * @param parentId  optional parent scope node ID
 * @param code      the uppercase node code
 * @param name      the human-readable name
 * @param path      the materialized ancestry path
 * @param createdAt creation timestamp
 * @param updatedAt last updated timestamp
 * @author edmaputra
 */
public record ScopeNodeResponse(
		String id,
		String tenantId,
		String parentId,
		String code,
		String name,
		String path,
		Instant createdAt,
		Instant updatedAt) {

	/**
	 * Maps a domain {@link ScopeNode} to a REST response DTO.
	 *
	 * @param node the domain scope node
	 * @return the response DTO
	 */
	public static ScopeNodeResponse from(ScopeNode node) {
		return new ScopeNodeResponse(
				node.getId().value().toString(),
				node.getTenantId().value().toString(),
				node.optionalParentId().map(p -> p.value().toString()).orElse(null),
				node.getCode(),
				node.getName(),
				node.getPath(),
				node.getCreatedAt(),
				node.getUpdatedAt());
	}
}
