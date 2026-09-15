package io.github.edmaputra.uwati.domain.tenancy.application.port.out;

import java.util.Optional;

import io.github.edmaputra.uwati.domain.tenancy.domain.Tenant;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;

/**
 * Outbound port (SPI) interface for persisting and loading {@link Tenant} aggregate roots.
 * <p>
 * Implemented by driven persistence adapters (e.g. JPA or relational database repositories)
 * in the outbound port layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public interface TenantRepository {

	/**
	 * Finds a tenant by its unique identifier.
	 *
	 * @param tenantId the identifier of the tenant
	 * @return an {@link Optional} containing the found {@link Tenant}, or empty if not found
	 */
	Optional<Tenant> findById(TenantId tenantId);

	/**
	 * Finds a tenant by its unique display name.
	 *
	 * @param displayName the display name of the tenant
	 * @return an {@link Optional} containing the matching {@link Tenant}, or empty if not found
	 */
	Optional<Tenant> findByDisplayName(String displayName);

	/**
	 * Persists a tenant entity, either by creating a new record or updating an existing one.
	 *
	 * @param tenant the tenant aggregate root to save
	 * @return the saved {@link Tenant} entity
	 */
	Tenant save(Tenant tenant);
}
