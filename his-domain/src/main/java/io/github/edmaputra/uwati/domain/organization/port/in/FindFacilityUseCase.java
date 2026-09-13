package io.github.edmaputra.uwati.domain.organization.port.in;

import java.util.List;
import java.util.Optional;

import io.github.edmaputra.uwati.domain.organization.Facility;
import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.FacilityStatus;
import io.github.edmaputra.uwati.domain.organization.FacilityType;

/**
 * Inbound port interface defining query operations for retrieving healthcare facilities.
 * <p>
 * Invoked by driving adapters to query facilities by identifier or filter criteria
 * within the application layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public interface FindFacilityUseCase {

	/**
	 * Finds a healthcare facility by its unique identifier.
	 *
	 * @param id the identifier of the facility
	 * @return an {@link Optional} containing the found {@link Facility}, or empty if not found
	 */
	Optional<Facility> findById(FacilityId id);

	/**
	 * Finds all healthcare facilities optionally filtered by type and operational status.
	 *
	 * @param type the facility type filter, or {@code null} to ignore type filtering
	 * @param status the facility status filter, or {@code null} to ignore status filtering
	 * @return the list of matching facilities, or empty list if none found
	 */
	List<Facility> findAll(FacilityType type, FacilityStatus status);
}
