package io.github.edmaputra.uwati.adapter.rest.organization;

import java.util.UUID;

import io.github.edmaputra.uwati.domain.organization.ServiceUnitId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitType;
import io.github.edmaputra.uwati.domain.organization.port.in.UpdateServiceUnitCommand;

public record UpdateServiceUnitRequest(
		String name,
		ServiceUnitType type,
		UUID scopeNodeId) {

	public UpdateServiceUnitCommand toCommand(ServiceUnitId id) {
		return new UpdateServiceUnitCommand(
				id,
				name,
				type,
				scopeNodeId);
	}
}
