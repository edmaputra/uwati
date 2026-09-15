package io.github.edmaputra.uwati.domain.organization.port.in;

import java.util.Objects;

import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.FacilityStatus;

/**
 * Inbound command encapsulating the parameters required to change a healthcare facility's operational status.
 * <p>
 * Passed across the inbound port boundary to {@link ChangeFacilityStatusUseCase}
 * in the application layer of the Hexagonal Architecture.
 *
 * @param id the unique identifier of the facility whose status is being changed
 * @param status the target operational status
 * @author edmaputra
 * @since 0.0.1
 */
public record ChangeFacilityStatusCommand(FacilityId id, FacilityStatus status) {

	/**
	 * Compact constructor validating invariant constraints for the status change command.
	 *
	 * @param id the facility ID
	 * @param status the facility status
	 * @throws NullPointerException if {@code id} or {@code status} is null
	 */
	public ChangeFacilityStatusCommand {
		Objects.requireNonNull(id, "Facility ID must not be null.");
		Objects.requireNonNull(status, "Facility status must not be null.");
	}
}
