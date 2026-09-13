package io.github.edmaputra.uwati.adapter.rest.organization;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.github.edmaputra.uwati.domain.organization.ServiceUnitId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitType;
import io.github.edmaputra.uwati.domain.organization.port.in.UpdateServiceUnitCommand;

/**
 * Request payload for updating service unit details.
 *
 * @param name updated service unit name
 * @param type updated functional unit classification
 * @param scopeNodeId updated hierarchical organizational scope node ID
 * @author edmaputra
 * @since 0.0.1
 */
public record UpdateServiceUnitRequest(
		@NotBlank(message = "Service unit name is required")
		@Size(max = 200, message = "Service unit name must not exceed 200 characters")
		String name,

		@NotNull(message = "Service unit type is required")
		ServiceUnitType type,

		UUID scopeNodeId) {

	/**
	 * Maps this validated request to an inbound domain command.
	 *
	 * @param id service unit identifier
	 * @return domain update command
	 */
	public UpdateServiceUnitCommand toCommand(ServiceUnitId id) {
		return new UpdateServiceUnitCommand(
				id,
				name,
				type,
				scopeNodeId);
	}
}
