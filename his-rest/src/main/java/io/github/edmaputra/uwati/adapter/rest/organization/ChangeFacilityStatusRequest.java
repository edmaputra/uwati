package io.github.edmaputra.uwati.adapter.rest.organization;

import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.FacilityStatus;
import io.github.edmaputra.uwati.domain.organization.port.in.ChangeFacilityStatusCommand;

public record ChangeFacilityStatusRequest(FacilityStatus status) {

	public ChangeFacilityStatusCommand toCommand(FacilityId id) {
		return new ChangeFacilityStatusCommand(id, status);
	}
}
