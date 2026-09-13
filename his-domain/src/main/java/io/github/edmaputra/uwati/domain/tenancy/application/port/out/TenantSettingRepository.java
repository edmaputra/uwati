package io.github.edmaputra.uwati.domain.tenancy.application.port.out;

import java.util.List;
import java.util.Optional;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;

/**
 * Outbound port (SPI) interface for storing and retrieving {@link TenantSetting} entities.
 * <p>
 * Implemented by driven persistence adapters in the outbound port layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public interface TenantSettingRepository {

	/**
	 * Retrieves all configuration settings belonging to the given tenant.
	 *
	 * @param tenantId the identifier of the tenant
	 * @return the list of settings for the specified tenant, or empty if none exist
	 */
	List<TenantSetting> findAllByTenantId(TenantId tenantId);

	/**
	 * Finds a specific setting by tenant ID and setting key.
	 *
	 * @param tenantId the identifier of the tenant
	 * @param key the setting key identifier
	 * @return an {@link Optional} containing the setting if found, or empty otherwise
	 */
	Optional<TenantSetting> findByTenantIdAndKey(TenantId tenantId, String key);

	/**
	 * Persists a collection of tenant settings in a batch.
	 *
	 * @param settings the list of {@link TenantSetting} records to save
	 * @return the list of persisted tenant settings
	 */
	List<TenantSetting> saveAll(List<TenantSetting> settings);
}
