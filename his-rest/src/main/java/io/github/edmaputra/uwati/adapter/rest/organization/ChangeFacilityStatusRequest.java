package io.github.edmaputra.uwati.adapter.rest.organization;

import jakarta.validation.constraints.NotNull;

import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.FacilityStatus;
import io.github.edmaputra.uwati.domain.organization.port.in.ChangeFacilityStatusCommand;

/**
 * Request payload for changing the operational lifecycle status of a facility.
 *
 * @param status target facility status
 * @author edmaputra
 * @since 0.0.1
 */
public record ChangeFacilityStatusRequest(
		@NotNull(message = "Facility status is required")
		FacilityStatus status) {

	/**
	 * Maps this validated request to an inbound domain command.
	 *
	 * @param id the facility identifier
	 * @return domain status change command
	 */
	public ChangeFacilityStatusCommand toCommand(FacilityId id) {
		return new ChangeFacilityStatusCommand(id, status);
	}
}
