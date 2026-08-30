package io.github.edmaputra.uwati.tenancy;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import io.github.edmaputra.uwati.TestcontainersConfiguration;
import io.github.edmaputra.uwati.bootstrap.UwatiApplication;
import io.github.edmaputra.uwati.cache.key.TenantAwareCacheKeyGenerator;
import io.github.edmaputra.uwati.cache.port.DistributedLockPort;
import io.github.edmaputra.uwati.cache.tenancy.CachedTenantSettingRegistry;
import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.ConfigureTenantSettingsCommand;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.ConfigureTenantSettingsUseCase;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.CreateTenantCommand;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.CreateTenantUseCase;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.GetTenantSettingsUseCase;
import io.github.edmaputra.uwati.domain.tenancy.domain.Tenant;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;
import io.github.edmaputra.uwati.test.RequiresDocker;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests verifying multi-tenant Valkey caching, tenant key isolation,
 * domain event-driven cache eviction, and distributed lock acquisition against Testcontainers.
 */
@RequiresDocker
@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = UwatiApplication.class)
@DisplayName("Integration: Multi-Tenant Redis Caching & Distributed Locks")
class TenantSettingsCachingIntegrationTests {

	@Autowired
	private CreateTenantUseCase createTenantUseCase;

	@Autowired
	private ConfigureTenantSettingsUseCase configureTenantSettingsUseCase;

	@Autowired
	private GetTenantSettingsUseCase getTenantSettingsUseCase;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private DistributedLockPort distributedLockPort;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void cleanUp() {
		jdbcTemplate.update("delete from audit_entries");
		jdbcTemplate.update("delete from tenant_document_sequences");
		jdbcTemplate.update("delete from tenant_settings");
		jdbcTemplate.update("delete from tenants");

		Cache cache = cacheManager.getCache(CachedTenantSettingRegistry.CACHE_NAME);
		if (cache != null) {
			cache.clear();
		}
	}

	@Test
	@DisplayName("Should cache tenant settings on read and evict on update")
	void shouldCacheTenantSettingsAndEvictOnUpdate() {
		OperationContext opContext = OperationContext.of("admin", "corr-cache-001");
		Tenant tenant = createTenantUseCase.execute(
				new CreateTenantCommand("Metropolitan Health Ltd", "Metro Health"),
				opContext);
		TenantId tenantId = tenant.id();

		String cacheKey = TenantAwareCacheKeyGenerator.formatTenantKey(tenantId, "all");
		Cache cache = cacheManager.getCache(CachedTenantSettingRegistry.CACHE_NAME);
		assertThat(cache).isNotNull();

		// 1. Initial state: cache is empty
		assertThat(cache.get(cacheKey)).isNull();

		// 2. First read: Misses cache, queries PostgreSQL, populates Redis cache
		List<TenantSetting> settings1 = getTenantSettingsUseCase.execute(tenantId);
		assertThat(settings1).isNotEmpty();
		assertThat(cache.get(cacheKey)).isNotNull();

		// 3. Second read: Hits Redis cache
		List<TenantSetting> settings2 = getTenantSettingsUseCase.execute(tenantId);
		assertThat(settings2).isEqualTo(settings1);

		// 4. Update setting: Triggers cache eviction via domain event listener
		configureTenantSettingsUseCase.execute(
				new ConfigureTenantSettingsCommand(
						tenantId,
						List.of(new ConfigureTenantSettingsCommand.SettingEntry("finance.currency", "IDR"))),
				opContext);

		// 5. Cache entry for this tenant must now be evicted
		assertThat(cache.get(cacheKey)).isNull();

		// 6. Next read: Repopulates cache with updated values
		List<TenantSetting> settingsAfterUpdate = getTenantSettingsUseCase.execute(tenantId);
		assertThat(settingsAfterUpdate)
				.anyMatch(s -> "finance.currency".equals(s.key()) && "IDR".equals(s.value()));
		assertThat(cache.get(cacheKey)).isNotNull();
	}

	@Test
	@DisplayName("Should isolate cache keys between different tenants")
	void shouldIsolateCacheKeysBetweenTenants() {
		OperationContext opContext = OperationContext.of("admin", "corr-iso-001");
		Tenant tenantA = createTenantUseCase.execute(new CreateTenantCommand("Hospital A", "Hosp A"), opContext);
		Tenant tenantB = createTenantUseCase.execute(new CreateTenantCommand("Hospital B", "Hosp B"), opContext);

		getTenantSettingsUseCase.execute(tenantA.id());
		getTenantSettingsUseCase.execute(tenantB.id());

		Cache cache = cacheManager.getCache(CachedTenantSettingRegistry.CACHE_NAME);
		assertThat(cache).isNotNull();

		String keyA = TenantAwareCacheKeyGenerator.formatTenantKey(tenantA.id(), "all");
		String keyB = TenantAwareCacheKeyGenerator.formatTenantKey(tenantB.id(), "all");

		assertThat(cache.get(keyA)).isNotNull();
		assertThat(cache.get(keyB)).isNotNull();
		assertThat(keyA).isNotEqualTo(keyB);
	}

	@Test
	@DisplayName("Should acquire and release distributed lock atomically")
	void shouldAcquireAndReleaseDistributedLock() {
		String lockKey = "uwati:lock:tenant:sequence:" + UUID.randomUUID();

		Optional<String> result = distributedLockPort.executeWithLock(lockKey, Duration.ofSeconds(5), () -> "LOCKED_TASK_SUCCESS");

		assertThat(result).contains("LOCKED_TASK_SUCCESS");

		// Lock should be released and available immediately
		boolean acquiredAgain = distributedLockPort.acquire(lockKey, "test-token", Duration.ofSeconds(2));
		assertThat(acquiredAgain).isTrue();

		distributedLockPort.release(lockKey, "test-token");
	}
}
