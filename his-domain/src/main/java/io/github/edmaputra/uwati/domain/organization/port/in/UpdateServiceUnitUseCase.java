package io.github.edmaputra.uwati.domain.organization.port.in;

import io.github.edmaputra.uwati.domain.organization.ServiceUnit;
import io.github.edmaputra.iam.domain.context.OperationContext;

/**
 * Inbound port interface defining the use case for updating profile details of a service unit.
 * <p>
 * Invoked by driving adapters to update service unit metadata and publish update events
 * within the application layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public interface UpdateServiceUnitUseCase {

	/**
	 * Executes the update of an existing service unit.
	 *
	 * @param command the command containing updated service unit attributes
	 * @param context the operation context with actor identity and tracing information
	 * @return the updated {@link ServiceUnit} entity
	 * @throws io.github.edmaputra.uwati.domain.organization.ServiceUnitNotFoundException if the service unit does not exist
	 */
	ServiceUnit execute(UpdateServiceUnitCommand command, OperationContext context);
}
