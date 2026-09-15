package io.github.edmaputra.uwati.domain.organization.port.out;

import java.util.List;
import java.util.Optional;

import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnit;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitStatus;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitType;

/**
 * Outbound port (SPI) interface for persisting and querying {@link ServiceUnit} aggregate roots.
 * <p>
 * Implemented by driven persistence adapters (e.g. relational JPA repositories)
 * in the outbound port layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public interface ServiceUnitRepository {

	/**
	 * Persists a service unit aggregate root, creating a new record or updating an existing one.
	 *
	 * @param serviceUnit the service unit entity to save
	 * @return the saved {@link ServiceUnit} entity
	 */
	ServiceUnit save(ServiceUnit serviceUnit);

	/**
	 * Finds a service unit by its unique identifier.
	 *
	 * @param id the identifier of the service unit
	 * @return an {@link Optional} containing the found service unit, or empty if not found
	 */
	Optional<ServiceUnit> findById(ServiceUnitId id);

	/**
	 * Finds a service unit by its facility ID and unique unit code.
	 *
	 * @param facilityId the identifier of the parent facility
	 * @param code the service unit code
	 * @return an {@link Optional} containing the matching service unit, or empty if not found
	 */
	Optional<ServiceUnit> findByFacilityIdAndCode(FacilityId facilityId, String code);

	/**
	 * Checks whether a service unit with the given code exists within the specified facility.
	 *
	 * @param facilityId the identifier of the parent facility
	 * @param code the service unit code to check
	 * @return {@code true} if a service unit exists with the code in the facility, {@code false} otherwise
	 */
	boolean existsByFacilityIdAndCode(FacilityId facilityId, String code);

	/**
	 * Finds all service units belonging to a facility matching optional type and status filter criteria.
	 *
	 * @param facilityId the identifier of the parent facility
	 * @param type the service unit type filter, or {@code null} to ignore type filtering
	 * @param status the service unit status filter, or {@code null} to ignore status filtering
	 * @return the list of matching service units, or empty list if none match
	 */
	List<ServiceUnit> findByFacilityId(FacilityId facilityId, ServiceUnitType type, ServiceUnitStatus status);
}
