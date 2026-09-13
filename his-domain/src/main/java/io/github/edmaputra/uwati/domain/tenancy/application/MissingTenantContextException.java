package io.github.edmaputra.uwati.domain.tenancy.application;

/**
 * Application exception thrown when a tenant-scoped operation is invoked without an active tenant context.
 * <p>
 * Enforces tenant context presence within the application layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public class MissingTenantContextException extends IllegalStateException {

	/**
	 * Constructs a new {@code MissingTenantContextException} with the specified detail message.
	 *
	 * @param message the detail explanation of the missing context
	 */
	public MissingTenantContextException(String message) {
		super(message);
	}
}
