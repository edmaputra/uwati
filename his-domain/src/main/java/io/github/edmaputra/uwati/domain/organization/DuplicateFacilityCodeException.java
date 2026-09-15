package io.github.edmaputra.uwati.domain.organization;

/**
 * Domain exception thrown when an attempt is made to create a facility with a code that already exists within the tenant.
 * <p>
 * Enforces facility code uniqueness constraints within the core domain layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public class DuplicateFacilityCodeException extends RuntimeException {

	/**
	 * Constructs a new {@code DuplicateFacilityCodeException} with the conflicting facility code.
	 *
	 * @param code the duplicate facility code
	 */
	public DuplicateFacilityCodeException(String code) {
		super("Facility code '" + code + "' already exists for this tenant.");
	}
}
