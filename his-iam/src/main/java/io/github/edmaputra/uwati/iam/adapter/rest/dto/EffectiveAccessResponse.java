package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import java.util.Set;
import java.util.stream.Collectors;

import io.github.edmaputra.uwati.iam.application.model.EffectiveAccess;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;
import io.github.edmaputra.uwati.iam.domain.model.UserId;

/**
 * REST response representing a diagnostic compilation of a user's effective permissions,
 * combined roles, and accessible scope hierarchy within a tenant.
 *
 * @param userId       the user ID
 * @param isTenantWide whether the user has tenant-wide access
 * @param roles        set of effective role codes
 * @param permissions  set of distinct effective permission codes
 * @param scopeNodeIds set of accessible scope node IDs
 * @param scopePaths   set of accessible materialized scope paths
 * @author edmaputra
 */
public record EffectiveAccessResponse(
		String userId,
		boolean isTenantWide,
		Set<String> roles,
		Set<String> permissions,
		Set<String> scopeNodeIds,
		Set<String> scopePaths) {

	/**
	 * Maps an {@link EffectiveAccess} calculation to a REST response DTO.
	 *
	 * @param userId the user ID
	 * @param access the effective access model
	 * @return the response DTO
	 */
	public static EffectiveAccessResponse from(UserId userId, EffectiveAccess access) {
		return new EffectiveAccessResponse(
				userId.value().toString(),
				access.tenantWide(),
				access.roles(),
				access.permissions(),
				access.accessibleScopeNodeIds().stream().map(Object::toString).collect(Collectors.toSet()),
				access.accessibleScopePaths());
	}
}
