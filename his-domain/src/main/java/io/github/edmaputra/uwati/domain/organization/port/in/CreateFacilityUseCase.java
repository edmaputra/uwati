package io.github.edmaputra.uwati.domain.organization.port.in;

import io.github.edmaputra.uwati.domain.organization.Facility;
import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;

/**
 * Inbound port interface defining the use case for registering a new healthcare facility.
 * <p>
 * Invoked by driving adapters to validate unique facility code constraints, generate identifiers,
 * and record facility creation within the application layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public interface CreateFacilityUseCase {

	/**
	 * Executes the registration of a new healthcare facility.
	 *
	 * @param command the command containing facility details
	 * @param context the operation context with actor identity and tracing information
	 * @return the newly created {@link Facility} entity
	 * @throws io.github.edmaputra.uwati.domain.organization.DuplicateFacilityCodeException if a facility with the same code already exists
	 */
	Facility execute(CreateFacilityCommand command, OperationContext context);
}
