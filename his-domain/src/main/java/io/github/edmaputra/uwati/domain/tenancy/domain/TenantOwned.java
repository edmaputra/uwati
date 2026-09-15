package io.github.edmaputra.uwati.domain.tenancy.domain;

/**
 * Contract for domain models and entities that belong to a specific tenant.
 * <p>
 * Ensures multi-tenant data isolation and tenancy-aware authorization across
 * the domain layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public interface TenantOwned {

	/**
	 * Returns the identifier of the tenant that owns this entity.
	 *
	 * @return the associated {@link TenantId}
	 */
	TenantId tenantId();
}
