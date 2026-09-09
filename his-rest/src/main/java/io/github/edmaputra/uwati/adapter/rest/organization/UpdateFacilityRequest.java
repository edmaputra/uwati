package io.github.edmaputra.uwati.adapter.rest.organization;

import java.util.UUID;

import io.github.edmaputra.uwati.domain.organization.FacilityClassification;
import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.FacilityType;
import io.github.edmaputra.uwati.domain.organization.port.in.UpdateFacilityCommand;

public record UpdateFacilityRequest(
		String name,
		FacilityType type,
		FacilityClassification classification,
		String nationalRegistryCode,
		UUID scopeNodeId,
		String address,
		String phone) {

	public UpdateFacilityCommand toCommand(FacilityId id) {
		return new UpdateFacilityCommand(
				id,
				name,
				type,
				classification,
				nationalRegistryCode,
				scopeNodeId,
				address,
				phone);
	}
}
