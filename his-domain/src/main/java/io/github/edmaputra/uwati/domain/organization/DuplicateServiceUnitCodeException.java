package io.github.edmaputra.uwati.domain.organization;

/**
 * Domain exception thrown when an attempt is made to create a service unit with a code that already exists within the facility.
 * <p>
 * Enforces service unit code uniqueness constraints per facility within the core domain layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public class DuplicateServiceUnitCodeException extends RuntimeException {

	/**
	 * Constructs a new {@code DuplicateServiceUnitCodeException} with the conflicting service unit code.
	 *
	 * @param code the duplicate service unit code
	 */
	public DuplicateServiceUnitCodeException(String code) {
		super("Service unit code '" + code + "' already exists for this facility.");
	}
}
