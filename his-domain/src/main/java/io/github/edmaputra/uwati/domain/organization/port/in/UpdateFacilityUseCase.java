package io.github.edmaputra.uwati.domain.organization.port.in;

import io.github.edmaputra.uwati.domain.organization.Facility;
import io.github.edmaputra.iam.domain.context.OperationContext;

/**
 * Inbound port interface defining the use case for updating details of an existing healthcare facility.
 * <p>
 * Invoked by driving adapters to modify facility attributes and record update events
 * within the application layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public interface UpdateFacilityUseCase {

	/**
	 * Executes the update of an existing healthcare facility.
	 *
	 * @param command the command containing updated facility fields
	 * @param context the operation context with actor identity and tracing information
	 * @return the updated {@link Facility} entity
	 * @throws io.github.edmaputra.uwati.domain.organization.FacilityNotFoundException if the facility does not exist
	 */
	Facility execute(UpdateFacilityCommand command, OperationContext context);
}
