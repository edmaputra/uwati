package io.github.edmaputra.uwati.cache.tenancy;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantSettingsUpdated;

/**
 * Inbound event listener adapter that invalidates tenant configuration caches upon updates.
 * <p>
 * Positioned in the caching adapter layer of the hexagonal architecture, this listener reacts
 * to {@link TenantSettingsUpdated} domain events by evicting stale Redis cache entries
 * via {@link CachedTenantSettingRegistry}, maintaining cache coherency across cluster nodes.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@Component
public class TenantSettingCacheEvictor {

	private static final Logger log = LoggerFactory.getLogger(TenantSettingCacheEvictor.class);

	private final CachedTenantSettingRegistry cachedRegistry;

	/**
	 * Constructs the cache evictor with the cached tenant setting registry decorator.
	 *
	 * @param cachedRegistry the cached registry decorator
	 * @throws NullPointerException if {@code cachedRegistry} is null
	 */
	public TenantSettingCacheEvictor(CachedTenantSettingRegistry cachedRegistry) {
		this.cachedRegistry = Objects.requireNonNull(cachedRegistry, "Cached registry must not be null.");
	}

	/**
	 * Reacts to tenant settings update events by invalidating cached entries.
	 *
	 * @param event the tenant settings updated domain event
	 */
	@EventListener
	public void onTenantSettingsUpdated(TenantSettingsUpdated event) {
		log.info("Received TenantSettingsUpdated event for tenant '{}'. Evicting cache.", event.tenantId());
		cachedRegistry.evictTenantSettings(event.tenantId());
	}
}
