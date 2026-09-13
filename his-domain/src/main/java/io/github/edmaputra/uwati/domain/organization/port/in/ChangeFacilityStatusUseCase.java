package io.github.edmaputra.uwati.domain.organization.port.in;

import io.github.edmaputra.uwati.domain.organization.Facility;
import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;

/**
 * Inbound port interface defining the use case for transitioning a facility's operational status.
 * <p>
 * Invoked by driving adapters to activate or deactivate a facility and emit status change events
 * within the application layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public interface ChangeFacilityStatusUseCase {

	/**
	 * Executes the status transition of a healthcare facility.
	 *
	 * @param command the command containing facility ID and target status
	 * @param context the operation context with actor identity and tracing information
	 * @return the facility entity with its updated status
	 * @throws io.github.edmaputra.uwati.domain.organization.FacilityNotFoundException if the facility does not exist
	 */
	Facility execute(ChangeFacilityStatusCommand command, OperationContext context);
}
