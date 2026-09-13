package io.github.edmaputra.uwati.domain.organization.port.in;

import io.github.edmaputra.uwati.domain.organization.ServiceUnit;
import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;

/**
 * Inbound port interface defining the use case for transitioning a service unit's operational status.
 * <p>
 * Invoked by driving adapters to activate or deactivate a service unit and publish status change events
 * within the application layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public interface ChangeServiceUnitStatusUseCase {

	/**
	 * Executes the status transition of a service unit.
	 *
	 * @param command the command containing service unit ID and new status
	 * @param context the operation context with actor identity and tracing information
	 * @return the service unit entity with its updated status
	 * @throws io.github.edmaputra.uwati.domain.organization.ServiceUnitNotFoundException if the service unit does not exist
	 */
	ServiceUnit execute(ChangeServiceUnitStatusCommand command, OperationContext context);
}
