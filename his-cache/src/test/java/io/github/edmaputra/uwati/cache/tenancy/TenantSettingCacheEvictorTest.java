package io.github.edmaputra.uwati.cache.tenancy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;
import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantSettingsUpdated;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TenantSettingCacheEvictorTest {

	@Test
	@DisplayName("Should evict tenant settings when TenantSettingsUpdated event is received")
	void shouldEvictSettingsOnEvent() {
		CachedTenantSettingRegistry cachedRegistry = mock(CachedTenantSettingRegistry.class);
		TenantSettingCacheEvictor evictor = new TenantSettingCacheEvictor(cachedRegistry);

		TenantId tenantId = new TenantId(UUID.randomUUID());
		TenantSettingsUpdated event = new TenantSettingsUpdated(
				tenantId,
				List.of(new TenantSetting(tenantId, "K1", "V1", 1)),
				List.of(new TenantSetting(tenantId, "K1", "V2", 2)),
				"operator-1",
				"corr-1",
				Instant.now());

		evictor.onTenantSettingsUpdated(event);

		verify(cachedRegistry).evictTenantSettings(tenantId);
	}
}
