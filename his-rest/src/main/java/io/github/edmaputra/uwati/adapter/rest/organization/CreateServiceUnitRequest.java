package io.github.edmaputra.uwati.adapter.rest.organization;

import java.util.UUID;

import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitType;
import io.github.edmaputra.uwati.domain.organization.port.in.CreateServiceUnitCommand;

public record CreateServiceUnitRequest(
		UUID facilityId,
		String code,
		String name,
		ServiceUnitType type,
		UUID scopeNodeId) {

	public CreateServiceUnitCommand toCommand() {
		return new CreateServiceUnitCommand(
				new FacilityId(facilityId),
				code,
				name,
				type,
				scopeNodeId);
	}
}
