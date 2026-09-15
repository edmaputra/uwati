package io.github.edmaputra.uwati.domain.organization.port.in;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitType;

/**
 * Inbound command encapsulating the parameters required to create a new service unit within a facility.
 * <p>
 * Passed across the inbound port boundary to {@link CreateServiceUnitUseCase}
 * in the application layer of the Hexagonal Architecture.
 *
 * @param facilityId the identifier of the facility that owns the service unit
 * @param code the unique code for the service unit within the facility
 * @param name the service unit name
 * @param type the service unit clinical or operational type
 * @param scopeNodeId the organizational tree scope node identifier, may be {@code null}
 * @author edmaputra
 * @since 0.0.1
 */
public record CreateServiceUnitCommand(
		FacilityId facilityId,
		String code,
		String name,
		ServiceUnitType type,
		UUID scopeNodeId) {

	/**
	 * Compact constructor validating invariant constraints for the service unit creation command.
	 *
	 * @param facilityId the facility ID
	 * @param code the service unit code
	 * @param name the service unit name
	 * @param type the service unit type
	 * @param scopeNodeId the scope node ID
	 * @throws NullPointerException if {@code facilityId} or {@code type} is null
	 * @throws IllegalArgumentException if {@code code} or {@code name} is null or blank
	 */
	public CreateServiceUnitCommand {
		Objects.requireNonNull(facilityId, "Facility ID must not be null.");
		if (code == null || code.isBlank()) {
			throw new IllegalArgumentException("Service unit code must not be blank.");
		}
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Service unit name must not be blank.");
		}
		Objects.requireNonNull(type, "Service unit type must not be null.");
	}
}
