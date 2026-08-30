package io.github.edmaputra.uwati.cache.tenancy;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import io.github.edmaputra.uwati.cache.key.TenantAwareCacheKeyGenerator;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantSettingRepository;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;

/**
 * Decorator implementing {@link TenantSettingRepository} with transparent Redis caching.
 * <p>
 * This hexagonal decorator wraps the primary JPA repository implementation ({@code JpaTenantSettingRegistry}),
 * preserving relational persistence purity while delivering sub-millisecond cached reads and deterministic cache eviction.
 */
@Primary
@Component
public class CachedTenantSettingRegistry implements TenantSettingRepository {

	private static final Logger log = LoggerFactory.getLogger(CachedTenantSettingRegistry.class);

	/**
	 * Name of the Redis cache for tenant settings.
	 */
	public static final String CACHE_NAME = "tenant_settings";

	private final TenantSettingRepository delegate;
	private final CacheManager cacheManager;

	/**
	 * Constructs the cached tenant setting registry decorator.
	 *
	 * @param delegate the underlying relational database repository
	 * @param cacheManager the Spring cache manager
	 */
	public CachedTenantSettingRegistry(
			@Qualifier("jpaTenantSettingRegistry") TenantSettingRepository delegate,
			CacheManager cacheManager) {
		this.delegate = Objects.requireNonNull(delegate, "Delegate repository must not be null.");
		this.cacheManager = Objects.requireNonNull(cacheManager, "Cache manager must not be null.");
	}

	/**
	 * Immutable wrapper record for caching collections of {@link TenantSetting} with full polymorphic type metadata.
	 *
	 * @param settings the unmodifiable list of tenant settings
	 */
	public record CachedTenantSettings(List<TenantSetting> settings) {
		public CachedTenantSettings {
			settings = settings != null ? List.copyOf(settings) : List.of();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<TenantSetting> findAllByTenantId(TenantId tenantId) {
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		String cacheKey = TenantAwareCacheKeyGenerator.formatTenantKey(tenantId, "all");
		Cache cache = getCache();

		if (cache != null) {
			try {
				Cache.ValueWrapper wrapper = cache.get(cacheKey);
				if (wrapper != null) {
					Object value = wrapper.get();
					if (value instanceof CachedTenantSettings cached) {
						log.debug("Cache hit for tenant settings: {}", cacheKey);
						return cached.settings();
					}
					if (value instanceof List<?> list) {
						log.debug("Cache hit for tenant settings (list): {}", cacheKey);
						return (List<TenantSetting>) list;
					}
				}
			}
			catch (Exception e) {
				log.warn("Cache read error for key '{}': {}", cacheKey, e.getMessage());
			}
		}

		log.debug("Cache miss for tenant settings: {}. Fetching from delegate.", cacheKey);
		List<TenantSetting> settings = delegate.findAllByTenantId(tenantId);

		if (cache != null) {
			try {
				cache.put(cacheKey, new CachedTenantSettings(settings));
			}
			catch (Exception e) {
				log.warn("Cache put error for key '{}': {}", cacheKey, e.getMessage());
			}
		}

		return settings;
	}

	@Override
	public Optional<TenantSetting> findByTenantIdAndKey(TenantId tenantId, String key) {
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		if (key == null || key.isBlank()) {
			return Optional.empty();
		}

		String cacheKey = TenantAwareCacheKeyGenerator.formatTenantKey(tenantId, key);
		Cache cache = getCache();

		if (cache != null) {
			try {
				Cache.ValueWrapper wrapper = cache.get(cacheKey);
				if (wrapper != null) {
					log.debug("Cache hit for tenant setting: {}", cacheKey);
					Object value = wrapper.get();
					return value instanceof TenantSetting setting ? Optional.of(setting) : Optional.empty();
				}
			}
			catch (Exception e) {
				log.warn("Cache read error for key '{}': {}", cacheKey, e.getMessage());
			}
		}

		log.debug("Cache miss for tenant setting: {}. Fetching from delegate.", cacheKey);
		Optional<TenantSetting> setting = delegate.findByTenantIdAndKey(tenantId, key);

		if (cache != null) {
			try {
				setting.ifPresent(s -> cache.put(cacheKey, s));
			}
			catch (Exception e) {
				log.warn("Cache put error for key '{}': {}", cacheKey, e.getMessage());
			}
		}

		return setting;
	}

	@Override
	public List<TenantSetting> saveAll(List<TenantSetting> settings) {
		Objects.requireNonNull(settings, "Settings must not be null.");
		List<TenantSetting> saved = delegate.saveAll(settings);

		// Invalidate cache for each modified tenant
		Cache cache = getCache();
		if (cache != null) {
			for (TenantSetting setting : settings) {
				evictTenantSettings(setting.tenantId());
			}
		}

		return saved;
	}

	/**
	 * Explicitly evicts all cached settings for the given tenant ID.
	 *
	 * @param tenantId the tenant ID whose cache entries should be evicted
	 */
	public void evictTenantSettings(TenantId tenantId) {
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		Cache cache = getCache();
		if (cache != null) {
			try {
				String allKey = TenantAwareCacheKeyGenerator.formatTenantKey(tenantId, "all");
				cache.evict(allKey);
				log.debug("Evicted cache for tenant settings: {}", allKey);
			}
			catch (Exception e) {
				log.warn("Failed to evict cache for tenant '{}': {}", tenantId, e.getMessage());
			}
		}
	}

	private Cache getCache() {
		return cacheManager.getCache(CACHE_NAME);
	}
}
