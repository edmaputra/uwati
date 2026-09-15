package io.github.edmaputra.uwati.domain.organization;

/**
 * Domain exception thrown when a requested facility cannot be found in the organization domain model.
 * <p>
 * Signals the absence of a requested facility entity within the core domain layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public class FacilityNotFoundException extends RuntimeException {

	/**
	 * Constructs a new {@code FacilityNotFoundException} with the identifier of the missing facility.
	 *
	 * @param id the identifier of the facility that was not found
	 */
	public FacilityNotFoundException(FacilityId id) {
		super("Facility not found with ID: " + id);
	}

	/**
	 * Constructs a new {@code FacilityNotFoundException} with the specified detail message.
	 *
	 * @param message the detail error message
	 */
	public FacilityNotFoundException(String message) {
		super(message);
	}
}
