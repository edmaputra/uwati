package io.github.edmaputra.uwati.domain.organization.port.in;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.organization.ServiceUnitId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitType;

/**
 * Inbound command encapsulating updated profile details for an existing service unit.
 * <p>
 * Passed across the inbound port boundary to {@link UpdateServiceUnitUseCase}
 * in the application layer of the Hexagonal Architecture.
 *
 * @param id the unique identifier of the service unit to update
 * @param name the updated name of the service unit
 * @param type the updated type of the service unit
 * @param scopeNodeId the updated organizational scope node ID, may be {@code null}
 * @author edmaputra
 * @since 0.0.1
 */
public record UpdateServiceUnitCommand(
		ServiceUnitId id,
		String name,
		ServiceUnitType type,
		UUID scopeNodeId) {

	/**
	 * Compact constructor validating invariant constraints for the service unit update command.
	 *
	 * @param id the service unit ID
	 * @param name the service unit name
	 * @param type the service unit type
	 * @param scopeNodeId the scope node ID
	 * @throws NullPointerException if {@code id} or {@code type} is null
	 * @throws IllegalArgumentException if {@code name} is null or blank
	 */
	public UpdateServiceUnitCommand {
		Objects.requireNonNull(id, "Service Unit ID must not be null.");
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Service unit name must not be blank.");
		}
		Objects.requireNonNull(type, "Service unit type must not be null.");
	}
}
