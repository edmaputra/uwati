package io.github.edmaputra.uwati.adapter.rest.organization;

import java.time.Instant;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.organization.Facility;
import io.github.edmaputra.uwati.domain.organization.FacilityClassification;
import io.github.edmaputra.uwati.domain.organization.FacilityStatus;
import io.github.edmaputra.uwati.domain.organization.FacilityType;

/**
 * Response payload representing healthcare facility details.
 *
 * @param id facility unique identifier
 * @param tenantId owning tenant identifier
 * @param code unique facility code within the tenant
 * @param name official facility name
 * @param type functional facility category
 * @param classification healthcare regulatory classification tier
 * @param nationalRegistryCode national health facility identifier
 * @param scopeNodeId hierarchical organizational scope node ID
 * @param address physical facility address
 * @param phone contact telephone number
 * @param status operational status
 * @param createdAt record creation timestamp
 * @param updatedAt record last update timestamp
 * @author edmaputra
 * @since 0.0.1
 */
public record FacilityResponse(
		UUID id,
		UUID tenantId,
		String code,
		String name,
		FacilityType type,
		FacilityClassification classification,
		String nationalRegistryCode,
		UUID scopeNodeId,
		String address,
		String phone,
		FacilityStatus status,
		Instant createdAt,
		Instant updatedAt) {

	/**
	 * Maps a domain Facility entity to a REST response DTO.
	 *
	 * @param facility the domain facility entity
	 * @return REST response DTO
	 */
	public static FacilityResponse from(Facility facility) {
		return new FacilityResponse(
				facility.id().value(),
				facility.tenantId().value(),
				facility.code(),
				facility.name(),
				facility.type(),
				facility.classification(),
				facility.nationalRegistryCode(),
				facility.scopeNodeId(),
				facility.address(),
				facility.phone(),
				facility.status(),
				facility.createdAt(),
				facility.updatedAt());
	}
}
