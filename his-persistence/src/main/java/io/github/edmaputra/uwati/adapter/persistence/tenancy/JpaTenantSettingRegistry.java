package io.github.edmaputra.uwati.adapter.persistence.tenancy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantSettingRepository;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;
import lombok.RequiredArgsConstructor;

/**
 * Secondary (outbound) persistence adapter implementing {@link TenantSettingRepository} using Spring Data JPA.
 * <p>
 * Bridges domain-driven tenant setting port interfaces to relational database persistence in the
 * hexagonal architecture. Handles querying and bulk upserting of tenant configuration parameters.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class JpaTenantSettingRegistry implements TenantSettingRepository {

	private final TenantSettingJpaRepository tenantSettings;

	/**
	 * Retrieves all configuration settings defined for a tenant.
	 *
	 * @param tenantId unique identifier of the tenant
	 * @return list of configuration settings for the specified tenant
	 * @throws NullPointerException if {@code tenantId} is null
	 */
	@Override
	public List<TenantSetting> findAllByTenantId(TenantId tenantId) {
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		return tenantSettings.findAllByTenantId(tenantId.value())
				.stream()
				.map(this::toDomain)
				.toList();
	}

	/**
	 * Finds a specific configuration setting by key for a tenant.
	 *
	 * @param tenantId unique identifier of the tenant
	 * @param key configuration key to find
	 * @return an {@link Optional} containing the tenant setting if found, or empty if not found or blank
	 * @throws NullPointerException if {@code tenantId} is null
	 */
	@Override
	public Optional<TenantSetting> findByTenantIdAndKey(TenantId tenantId, String key) {
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		if (key == null || key.isBlank()) {
			return Optional.empty();
		}
		return tenantSettings.findByTenantIdAndSettingKey(tenantId.value(), key)
				.map(this::toDomain);
	}

	/**
	 * Persists or updates a collection of tenant configuration settings.
	 *
	 * @param settings collection of tenant settings to persist or update
	 * @return unmodifiable list of persisted tenant settings
	 * @throws NullPointerException if {@code settings} is null
	 */
	@Override
	public List<TenantSetting> saveAll(List<TenantSetting> settings) {
		Objects.requireNonNull(settings, "Settings must not be null.");
		List<TenantSetting> results = new ArrayList<>();
		for (TenantSetting setting : settings) {
			Optional<TenantSettingEntity> existing =
					tenantSettings.findByTenantIdAndSettingKey(setting.tenantId().value(), setting.key());

			TenantSettingEntity entity;
			if (existing.isPresent()) {
				entity = existing.get();
				entity.update(setting.value(), setting.revision());
			}
			else {
				entity = toEntity(setting);
			}

			results.add(toDomain(tenantSettings.save(entity)));
		}
		return List.copyOf(results);
	}

	private TenantSetting toDomain(TenantSettingEntity entity) {
		return new TenantSetting(
				new TenantId(entity.tenantId()),
				entity.settingKey(),
				entity.settingValue(),
				entity.revision());
	}

	private TenantSettingEntity toEntity(TenantSetting setting) {
		return new TenantSettingEntity(
				setting.tenantId().value(),
				setting.key(),
				setting.value(),
				setting.revision());
	}
}
