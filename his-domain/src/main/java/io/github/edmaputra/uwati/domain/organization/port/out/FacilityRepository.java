package io.github.edmaputra.uwati.domain.organization.port.out;

import java.util.List;
import java.util.Optional;

import io.github.edmaputra.uwati.domain.organization.Facility;
import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.FacilityStatus;
import io.github.edmaputra.uwati.domain.organization.FacilityType;

/**
 * Outbound port (SPI) interface for persisting and loading {@link Facility} aggregate roots.
 * <p>
 * Implemented by driven persistence adapters (e.g. relational JPA repositories)
 * in the outbound port layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public interface FacilityRepository {

	/**
	 * Persists a facility aggregate root, creating a new record or updating an existing one.
	 *
	 * @param facility the facility entity to persist
	 * @return the saved {@link Facility} entity
	 */
	Facility save(Facility facility);

	/**
	 * Finds a facility by its unique identifier.
	 *
	 * @param id the identifier of the facility
	 * @return an {@link Optional} containing the found facility, or empty if not found
	 */
	Optional<Facility> findById(FacilityId id);

	/**
	 * Finds a facility by its unique facility code.
	 *
	 * @param code the unique facility code
	 * @return an {@link Optional} containing the found facility, or empty if not found
	 */
	Optional<Facility> findByCode(String code);

	/**
	 * Checks whether a facility with the specified code exists in the current tenant scope.
	 *
	 * @param code the facility code to check
	 * @return {@code true} if a facility exists with the code, {@code false} otherwise
	 */
	boolean existsByCode(String code);

	/**
	 * Finds all facilities matching optional type and status filter criteria.
	 *
	 * @param type the facility type filter, or {@code null} to ignore type filtering
	 * @param status the facility status filter, or {@code null} to ignore status filtering
	 * @return the list of matching facilities, or empty list if none match
	 */
	List<Facility> findAll(FacilityType type, FacilityStatus status);
}
