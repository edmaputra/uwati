package io.github.edmaputra.uwati.adapter.rest.organization;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.github.edmaputra.uwati.domain.organization.FacilityClassification;
import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.FacilityType;
import io.github.edmaputra.uwati.domain.organization.port.in.UpdateFacilityCommand;

/**
 * Request payload for updating facility details.
 *
 * @param name updated facility name
 * @param type updated facility type
 * @param classification updated regulatory classification tier
 * @param nationalRegistryCode updated national health facility identifier
 * @param scopeNodeId updated hierarchical organizational scope node ID
 * @param address updated physical address
 * @param phone updated contact telephone number
 * @author edmaputra
 * @since 0.0.1
 */
public record UpdateFacilityRequest(
		@NotBlank(message = "Facility name is required")
		@Size(max = 200, message = "Facility name must not exceed 200 characters")
		String name,

		@NotNull(message = "Facility type is required")
		FacilityType type,

		@NotNull(message = "Facility classification is required")
		FacilityClassification classification,

		@Size(max = 100, message = "National registry code must not exceed 100 characters")
		String nationalRegistryCode,

		UUID scopeNodeId,

		String address,

		@Size(max = 50, message = "Phone number must not exceed 50 characters")
		String phone) {

	/**
	 * Maps this validated request to an inbound domain command.
	 *
	 * @param id the facility identifier to update
	 * @return domain update command
	 */
	public UpdateFacilityCommand toCommand(FacilityId id) {
		return new UpdateFacilityCommand(
				id,
				name,
				type,
				classification,
				nationalRegistryCode,
				scopeNodeId,
				address,
				phone);
	}
}
