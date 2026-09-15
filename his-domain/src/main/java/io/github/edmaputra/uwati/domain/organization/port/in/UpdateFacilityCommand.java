package io.github.edmaputra.uwati.domain.organization.port.in;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.organization.FacilityClassification;
import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.FacilityType;

/**
 * Inbound command encapsulating updated profile details for an existing healthcare facility.
 * <p>
 * Passed across the inbound port boundary to {@link UpdateFacilityUseCase}
 * in the application layer of the Hexagonal Architecture.
 *
 * @param id the unique identifier of the facility to update
 * @param name the updated facility name
 * @param type the updated facility type
 * @param classification the updated classification
 * @param nationalRegistryCode the updated national registry code, may be {@code null}
 * @param scopeNodeId the updated organizational scope node ID, may be {@code null}
 * @param address the updated street address, may be {@code null}
 * @param phone the updated telephone number, may be {@code null}
 * @author edmaputra
 * @since 0.0.1
 */
public record UpdateFacilityCommand(
		FacilityId id,
		String name,
		FacilityType type,
		FacilityClassification classification,
		String nationalRegistryCode,
		UUID scopeNodeId,
		String address,
		String phone) {

	/**
	 * Compact constructor validating invariant constraints for the facility update command.
	 *
	 * @param id the facility ID
	 * @param name the facility name
	 * @param type the facility type
	 * @param classification the facility classification
	 * @param nationalRegistryCode the national registry code
	 * @param scopeNodeId the scope node ID
	 * @param address the facility address
	 * @param phone the facility phone
	 * @throws NullPointerException if {@code id}, {@code type}, or {@code classification} is null
	 * @throws IllegalArgumentException if {@code name} is null or blank
	 */
	public UpdateFacilityCommand {
		Objects.requireNonNull(id, "Facility ID must not be null.");
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Facility name must not be blank.");
		}
		Objects.requireNonNull(type, "Facility type must not be null.");
		Objects.requireNonNull(classification, "Facility classification must not be null.");
	}
}
