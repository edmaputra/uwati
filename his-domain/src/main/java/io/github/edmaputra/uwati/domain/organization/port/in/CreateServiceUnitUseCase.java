package io.github.edmaputra.uwati.domain.organization.port.in;

import io.github.edmaputra.uwati.domain.organization.ServiceUnit;
import io.github.edmaputra.iam.domain.context.OperationContext;

/**
 * Inbound port interface defining the use case for creating a service unit within a facility.
 * <p>
 * Invoked by driving adapters to ensure code uniqueness within the facility, instantiate service units,
 * and publish domain events in the application layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public interface CreateServiceUnitUseCase {

	/**
	 * Executes the creation of a new service unit within a facility.
	 *
	 * @param command the command containing service unit details
	 * @param context the operation context with actor identity and tracing information
	 * @return the newly created {@link ServiceUnit} entity
	 * @throws io.github.edmaputra.uwati.domain.organization.DuplicateServiceUnitCodeException if a service unit with the same code already exists for the facility
	 * @throws io.github.edmaputra.uwati.domain.organization.FacilityNotFoundException if the target facility does not exist
	 */
	ServiceUnit execute(CreateServiceUnitCommand command, OperationContext context);
}
