package io.github.edmaputra.uwati.cache.tenancy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import io.github.edmaputra.uwati.cache.key.TenantAwareCacheKeyGenerator;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantSettingRepository;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CachedTenantSettingRegistryTest {

	private TenantSettingRepository delegate;
	private CacheManager cacheManager;
	private Cache cache;
	private CachedTenantSettingRegistry cachedRegistry;

	@BeforeEach
	void setUp() {
		delegate = mock(TenantSettingRepository.class);
		cacheManager = mock(CacheManager.class);
		cache = mock(Cache.class);

		when(cacheManager.getCache(CachedTenantSettingRegistry.CACHE_NAME)).thenReturn(cache);
		cachedRegistry = new CachedTenantSettingRegistry(delegate, cacheManager);
	}

	@Test
	@DisplayName("Should return settings from cache on cache hit without calling delegate")
	void shouldReturnFromCacheOnHit() {
		TenantId tenantId = new TenantId(UUID.randomUUID());
		String cacheKey = TenantAwareCacheKeyGenerator.formatTenantKey(tenantId, "all");
		List<TenantSetting> cachedSettings = List.of(
				new TenantSetting(tenantId, "KEY1", "VAL1", 1)
		);

		Cache.ValueWrapper wrapper = mock(Cache.ValueWrapper.class);
		when(wrapper.get()).thenReturn(cachedSettings);
		when(cache.get(cacheKey)).thenReturn(wrapper);

		List<TenantSetting> result = cachedRegistry.findAllByTenantId(tenantId);

		assertThat(result).isEqualTo(cachedSettings);
		verify(delegate, never()).findAllByTenantId(any());
	}

	@Test
	@DisplayName("Should fetch from delegate and populate cache on cache miss")
	void shouldFetchFromDelegateAndPopulateCacheOnMiss() {
		TenantId tenantId = new TenantId(UUID.randomUUID());
		String cacheKey = TenantAwareCacheKeyGenerator.formatTenantKey(tenantId, "all");
		List<TenantSetting> dbSettings = List.of(
				new TenantSetting(tenantId, "KEY1", "VAL1", 1)
		);

		when(cache.get(cacheKey)).thenReturn(null);
		when(delegate.findAllByTenantId(tenantId)).thenReturn(dbSettings);

		List<TenantSetting> result = cachedRegistry.findAllByTenantId(tenantId);

		assertThat(result).isEqualTo(dbSettings);
		verify(delegate).findAllByTenantId(tenantId);
		verify(cache).put(cacheKey, dbSettings);
	}

	@Test
	@DisplayName("Should return setting by key from cache on hit")
	void shouldReturnSettingByKeyFromCacheOnHit() {
		TenantId tenantId = new TenantId(UUID.randomUUID());
		String cacheKey = TenantAwareCacheKeyGenerator.formatTenantKey(tenantId, "KEY1");
		TenantSetting cachedSetting = new TenantSetting(tenantId, "KEY1", "VAL1", 1);

		Cache.ValueWrapper wrapper = mock(Cache.ValueWrapper.class);
		when(wrapper.get()).thenReturn(cachedSetting);
		when(cache.get(cacheKey)).thenReturn(wrapper);

		Optional<TenantSetting> result = cachedRegistry.findByTenantIdAndKey(tenantId, "KEY1");

		assertThat(result).contains(cachedSetting);
		verify(delegate, never()).findByTenantIdAndKey(any(), any());
	}

	@Test
	@DisplayName("Should invalidate cache on saveAll")
	void shouldInvalidateCacheOnSaveAll() {
		TenantId tenantId = new TenantId(UUID.randomUUID());
		List<TenantSetting> settingsToSave = List.of(
				new TenantSetting(tenantId, "KEY1", "NEW_VAL", 2)
		);

		when(delegate.saveAll(settingsToSave)).thenReturn(settingsToSave);

		List<TenantSetting> result = cachedRegistry.saveAll(settingsToSave);

		assertThat(result).isEqualTo(settingsToSave);
		verify(delegate).saveAll(settingsToSave);
		verify(cache).evict(TenantAwareCacheKeyGenerator.formatTenantKey(tenantId, "all"));
	}
}
