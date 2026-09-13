package io.github.edmaputra.uwati.domain.organization.port.in;

import java.util.Objects;

import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.FacilityStatus;

public record ChangeFacilityStatusCommand(FacilityId id, FacilityStatus status) {

	public ChangeFacilityStatusCommand {
		Objects.requireNonNull(id, "Facility ID must not be null.");
		Objects.requireNonNull(status, "Facility status must not be null.");
	}
}
