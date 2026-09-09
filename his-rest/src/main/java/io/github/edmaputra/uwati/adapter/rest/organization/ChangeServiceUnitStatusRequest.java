package io.github.edmaputra.uwati.adapter.rest.organization;

import io.github.edmaputra.uwati.domain.organization.ServiceUnitId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitStatus;
import io.github.edmaputra.uwati.domain.organization.port.in.ChangeServiceUnitStatusCommand;

public record ChangeServiceUnitStatusRequest(ServiceUnitStatus status) {

	public ChangeServiceUnitStatusCommand toCommand(ServiceUnitId id) {
		return new ChangeServiceUnitStatusCommand(id, status);
	}
}
