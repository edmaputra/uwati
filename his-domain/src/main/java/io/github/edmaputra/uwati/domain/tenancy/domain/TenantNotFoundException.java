package io.github.edmaputra.uwati.domain.tenancy.domain;

/**
 * Domain exception thrown when a requested tenant cannot be found in the domain model.
 * <p>
 * Signals the absence of a requested tenant entity within the core domain layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public class TenantNotFoundException extends IllegalArgumentException {

	/**
	 * Constructs a new {@code TenantNotFoundException} with the identifier of the missing tenant.
	 *
	 * @param tenantId the identifier of the tenant that was not found
	 */
	public TenantNotFoundException(TenantId tenantId) {
		super("Tenant with ID '%s' was not found.".formatted(tenantId));
	}
}
