package io.github.edmaputra.uwati.adapter.rest.organization;

import jakarta.validation.constraints.NotNull;

import io.github.edmaputra.uwati.domain.organization.ServiceUnitId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitStatus;
import io.github.edmaputra.uwati.domain.organization.port.in.ChangeServiceUnitStatusCommand;

/**
 * Request payload for changing the operational lifecycle status of a service unit.
 *
 * @param status target service unit status
 * @author edmaputra
 * @since 0.0.1
 */
public record ChangeServiceUnitStatusRequest(
		@NotNull(message = "Service unit status is required")
		ServiceUnitStatus status) {

	/**
	 * Maps this validated request to an inbound domain command.
	 *
	 * @param id the service unit identifier
	 * @return domain status change command
	 */
	public ChangeServiceUnitStatusCommand toCommand(ServiceUnitId id) {
		return new ChangeServiceUnitStatusCommand(id, status);
	}
}
