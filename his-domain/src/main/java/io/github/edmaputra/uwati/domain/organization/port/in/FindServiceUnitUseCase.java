package io.github.edmaputra.uwati.domain.organization.port.in;

import java.util.List;
import java.util.Optional;

import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnit;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitStatus;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitType;

/**
 * Inbound port interface defining query operations for retrieving service units.
 * <p>
 * Invoked by driving adapters to query service units by identifier or facility with optional filters
 * within the application layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public interface FindServiceUnitUseCase {

	/**
	 * Finds a service unit by its unique identifier.
	 *
	 * @param id the identifier of the service unit
	 * @return an {@link Optional} containing the found {@link ServiceUnit}, or empty if not found
	 */
	Optional<ServiceUnit> findById(ServiceUnitId id);

	/**
	 * Finds all service units belonging to a specific facility, optionally filtered by type and status.
	 *
	 * @param facilityId the identifier of the parent facility
	 * @param type the service unit type filter, or {@code null} to ignore type filtering
	 * @param status the service unit status filter, or {@code null} to ignore status filtering
	 * @return the list of matching service units, or empty list if none found
	 */
	List<ServiceUnit> findByFacilityId(FacilityId facilityId, ServiceUnitType type, ServiceUnitStatus status);
}
