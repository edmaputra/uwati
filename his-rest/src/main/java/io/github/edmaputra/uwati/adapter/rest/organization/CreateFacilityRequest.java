package io.github.edmaputra.uwati.adapter.rest.organization;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.github.edmaputra.uwati.domain.organization.FacilityClassification;
import io.github.edmaputra.uwati.domain.organization.FacilityType;
import io.github.edmaputra.uwati.domain.organization.port.in.CreateFacilityCommand;

/**
 * Request payload for creating a healthcare facility.
 *
 * @param code unique facility code within the tenant
 * @param name official facility name
 * @param type facility functional category
 * @param classification healthcare regulatory classification tier
 * @param nationalRegistryCode national health facility identifier
 * @param scopeNodeId hierarchical organizational scope node ID
 * @param address physical address
 * @param phone contact telephone number
 * @author edmaputra
 * @since 0.0.1
 */
public record CreateFacilityRequest(
		@NotBlank(message = "Facility code is required")
		@Size(max = 50, message = "Facility code must not exceed 50 characters")
		String code,

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
	 * @return domain creation command
	 */
	public CreateFacilityCommand toCommand() {
		return new CreateFacilityCommand(
				code,
				name,
				type,
				classification,
				nationalRegistryCode,
				scopeNodeId,
				address,
				phone);
	}
}
