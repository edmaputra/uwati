package io.github.edmaputra.uwati.adapter.persistence.tenancy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for tenant settings relational database operations.
 * <p>
 * Functions as an internal persistence mechanism in the hexagonal architecture's
 * outbound tenancy persistence adapter, providing database access for tenant key-value configurations.
 *
 * @author edmaputra
 * @since 0.0.1
 */
interface TenantSettingJpaRepository extends JpaRepository<TenantSettingEntity, Long> {

	/**
	 * Finds all setting entities belonging to the specified tenant.
	 *
	 * @param tenantId unique identifier of the tenant
	 * @return list of tenant setting entities
	 */
	List<TenantSettingEntity> findAllByTenantId(UUID tenantId);

	/**
	 * Finds a setting entity by tenant ID and setting key.
	 *
	 * @param tenantId unique identifier of the tenant
	 * @param settingKey configuration setting key
	 * @return an {@link Optional} containing the setting entity if found, or empty otherwise
	 */
	Optional<TenantSettingEntity> findByTenantIdAndSettingKey(UUID tenantId, String settingKey);
}
