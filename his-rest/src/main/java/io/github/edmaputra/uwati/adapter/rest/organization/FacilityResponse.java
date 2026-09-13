package io.github.edmaputra.uwati.adapter.rest.organization;

import java.time.Instant;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.organization.Facility;
import io.github.edmaputra.uwati.domain.organization.FacilityClassification;
import io.github.edmaputra.uwati.domain.organization.FacilityStatus;
import io.github.edmaputra.uwati.domain.organization.FacilityType;

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
