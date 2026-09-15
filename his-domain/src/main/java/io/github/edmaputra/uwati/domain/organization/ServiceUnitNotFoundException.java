package io.github.edmaputra.uwati.domain.organization;

/**
 * Domain exception thrown when a requested service unit cannot be found in the organization domain model.
 * <p>
 * Signals the absence of a requested service unit entity within the core domain layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public class ServiceUnitNotFoundException extends RuntimeException {

	/**
	 * Constructs a new {@code ServiceUnitNotFoundException} with the identifier of the missing service unit.
	 *
	 * @param id the identifier of the service unit that was not found
	 */
	public ServiceUnitNotFoundException(ServiceUnitId id) {
		super("Service unit not found with ID: " + id);
	}

	/**
	 * Constructs a new {@code ServiceUnitNotFoundException} with the specified detail message.
	 *
	 * @param message the detail error message
	 */
	public ServiceUnitNotFoundException(String message) {
		super(message);
	}
}
