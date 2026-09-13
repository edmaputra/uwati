package io.github.edmaputra.uwati.domain.organization.port.in;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.organization.FacilityClassification;
import io.github.edmaputra.uwati.domain.organization.FacilityType;

/**
 * Inbound command encapsulating the parameters required to register a new healthcare facility.
 * <p>
 * Passed across the inbound port boundary to {@link CreateFacilityUseCase}
 * in the application layer of the Hexagonal Architecture.
 *
 * @param code the unique code identifying the facility within the tenant
 * @param name the facility name
 * @param type the type of facility (hospital, clinic, etc.)
 * @param classification the accreditation/regulatory classification
 * @param nationalRegistryCode the official national registry code, may be {@code null}
 * @param scopeNodeId the organizational tree scope node identifier, may be {@code null}
 * @param address the physical street address, may be {@code null}
 * @param phone the contact phone number, may be {@code null}
 * @author edmaputra
 * @since 0.0.1
 */
public record CreateFacilityCommand(
		String code,
		String name,
		FacilityType type,
		FacilityClassification classification,
		String nationalRegistryCode,
		UUID scopeNodeId,
		String address,
		String phone) {

	/**
	 * Compact constructor validating invariant constraints for the facility creation command.
	 *
	 * @param code the facility code
	 * @param name the facility name
	 * @param type the facility type
	 * @param classification the facility classification
	 * @param nationalRegistryCode the national registry code
	 * @param scopeNodeId the scope node ID
	 * @param address the facility address
	 * @param phone the facility phone
	 * @throws IllegalArgumentException if {@code code} or {@code name} is null or blank
	 * @throws NullPointerException if {@code type} or {@code classification} is null
	 */
	public CreateFacilityCommand {
		if (code == null || code.isBlank()) {
			throw new IllegalArgumentException("Facility code must not be blank.");
		}
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Facility name must not be blank.");
		}
		Objects.requireNonNull(type, "Facility type must not be null.");
		Objects.requireNonNull(classification, "Facility classification must not be null.");
	}
}
