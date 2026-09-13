package io.github.edmaputra.uwati.adapter.rest.organization;

import java.util.UUID;

import io.github.edmaputra.uwati.domain.organization.FacilityClassification;
import io.github.edmaputra.uwati.domain.organization.FacilityType;
import io.github.edmaputra.uwati.domain.organization.port.in.CreateFacilityCommand;

public record CreateFacilityRequest(
		String code,
		String name,
		FacilityType type,
		FacilityClassification classification,
		String nationalRegistryCode,
		UUID scopeNodeId,
		String address,
		String phone) {

	public CreateFacilityCommand toCommand() {
		return new CreateFacilityCommand(
				code,
				name,
				type,
				classification,
				nationalRegistryCode,
				scopeNodeId,
				address,
				phone);
	}
}
