package io.github.edmaputra.uwati.adapter.rest.organization;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitType;
import io.github.edmaputra.uwati.domain.organization.port.in.CreateServiceUnitCommand;

/**
 * Request payload for creating a healthcare service unit within a facility.
 *
 * @param facilityId parent facility identifier
 * @param code unique service unit code within the tenant
 * @param name official service unit name
 * @param type functional unit classification
 * @param scopeNodeId hierarchical organizational scope node ID
 * @author edmaputra
 * @since 0.0.1
 */
public record CreateServiceUnitRequest(
		@NotNull(message = "Facility ID is required")
		UUID facilityId,

		@NotBlank(message = "Service unit code is required")
		@Size(max = 50, message = "Service unit code must not exceed 50 characters")
		String code,

		@NotBlank(message = "Service unit name is required")
		@Size(max = 200, message = "Service unit name must not exceed 200 characters")
		String name,

		@NotNull(message = "Service unit type is required")
		ServiceUnitType type,

		UUID scopeNodeId) {

	/**
	 * Maps this validated request to an inbound domain command.
	 *
	 * @return domain creation command
	 */
	public CreateServiceUnitCommand toCommand() {
		return new CreateServiceUnitCommand(
				new FacilityId(facilityId),
				code,
				name,
				type,
				scopeNodeId);
	}
}
