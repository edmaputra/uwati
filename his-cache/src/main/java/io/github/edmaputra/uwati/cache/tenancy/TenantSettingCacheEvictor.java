package io.github.edmaputra.uwati.cache.tenancy;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantSettingsUpdated;

@Component
public class TenantSettingCacheEvictor {

	private static final Logger log = LoggerFactory.getLogger(TenantSettingCacheEvictor.class);

	private final CachedTenantSettingRegistry cachedRegistry;

	public TenantSettingCacheEvictor(CachedTenantSettingRegistry cachedRegistry) {
		this.cachedRegistry = Objects.requireNonNull(cachedRegistry, "Cached registry must not be null.");
	}

	@EventListener
	public void onTenantSettingsUpdated(TenantSettingsUpdated event) {
		log.info("Received TenantSettingsUpdated event for tenant '{}'. Evicting cache.", event.tenantId());
		cachedRegistry.evictTenantSettings(event.tenantId());
	}
}
