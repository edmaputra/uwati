package io.github.edmaputra.uwati.domain.organization.port.in;

import java.util.Objects;

import io.github.edmaputra.uwati.domain.organization.ServiceUnitId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitStatus;

/**
 * Inbound command encapsulating the parameters required to change a service unit's operational status.
 * <p>
 * Passed across the inbound port boundary to {@link ChangeServiceUnitStatusUseCase}
 * in the application layer of the Hexagonal Architecture.
 *
 * @param id the unique identifier of the service unit whose status is being changed
 * @param status the target operational status
 * @author edmaputra
 * @since 0.0.1
 */
public record ChangeServiceUnitStatusCommand(ServiceUnitId id, ServiceUnitStatus status) {

	/**
	 * Compact constructor validating invariant constraints for the service unit status change command.
	 *
	 * @param id the service unit ID
	 * @param status the service unit status
	 * @throws NullPointerException if {@code id} or {@code status} is null
	 */
	public ChangeServiceUnitStatusCommand {
		Objects.requireNonNull(id, "Service Unit ID must not be null.");
		Objects.requireNonNull(status, "Service Unit status must not be null.");
	}
}
