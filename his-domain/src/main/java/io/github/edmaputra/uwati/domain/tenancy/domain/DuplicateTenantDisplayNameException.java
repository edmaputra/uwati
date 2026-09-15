package io.github.edmaputra.uwati.domain.tenancy.domain;

/**
 * Domain exception thrown when an attempt is made to create or rename a tenant with a display name that is already in use.
 * <p>
 * Enforces tenant display name uniqueness within the core domain layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public class DuplicateTenantDisplayNameException extends IllegalArgumentException {

	/**
	 * Constructs a new {@code DuplicateTenantDisplayNameException} with the conflicting display name.
	 *
	 * @param displayName the duplicated display name that triggered the exception
	 */
	public DuplicateTenantDisplayNameException(String displayName) {
		super("A tenant with the display name '%s' already exists.".formatted(displayName));
	}
}
