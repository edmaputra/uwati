package io.github.edmaputra.uwati.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.github.edmaputra.uwati.cache.adapter.RedisDistributedLockAdapter;
import io.github.edmaputra.uwati.cache.config.RedisCacheProperties;
import io.github.edmaputra.uwati.cache.error.ResilienceCacheErrorHandler;
import io.github.edmaputra.uwati.cache.key.TenantAwareCacheKeyGenerator;
import io.github.edmaputra.uwati.cache.port.DistributedLockPort;
import io.github.edmaputra.uwati.cache.serialization.JacksonRedisSerializerFactory;
import io.github.edmaputra.uwati.domain.tenancy.application.TenantContext;

/**
 * Spring Boot Auto-Configuration for distributed Redis/Valkey caching and concurrency control.
 * <p>
 * This configuration automatically provisions:
 * <ul>
 *   <li>{@link TenantAwareCacheKeyGenerator} for tenant-isolated cache key namespacing.</li>
 *   <li>{@link ResilienceCacheErrorHandler} for non-blocking cache error fallback.</li>
 *   <li>{@link RedisCacheManager} pre-configured with Jackson 3 serialization and per-cache TTLs.</li>
 *   <li>{@link RedisTemplate} for direct Redis key-value interactions.</li>
 *   <li>{@link DistributedLockPort} backed by {@link RedisDistributedLockAdapter} for atomic locking.</li>
 * </ul>
 */
@AutoConfiguration
@EnableCaching
@ConditionalOnClass(RedisConnectionFactory.class)
@ConditionalOnProperty(prefix = "uwati.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RedisCacheProperties.class)
public class CacheAutoConfiguration implements CachingConfigurer {

	/**
	 * Creates a tenant-aware {@link KeyGenerator} that prefixes cache keys with the active tenant ID.
	 *
	 * @param tenantContext optional tenant context accessor
	 * @return tenant-aware KeyGenerator
	 */
	@Bean
	@ConditionalOnMissingBean(name = "tenantAwareKeyGenerator")
	public KeyGenerator tenantAwareKeyGenerator(Optional<TenantContext> tenantContext) {
		return new TenantAwareCacheKeyGenerator(tenantContext);
	}

	/**
	 * Creates a resilient {@link CacheErrorHandler} that logs warnings and falls back to primary storage
	 * rather than failing application requests when cache operations error.
	 *
	 * @return resilient CacheErrorHandler
	 */
	@Bean
	@ConditionalOnMissingBean
	public CacheErrorHandler cacheErrorHandler() {
		return new ResilienceCacheErrorHandler();
	}

	/**
	 * Configures the default Spring Cache error handler for declarative caching annotations.
	 *
	 * @return the resilient CacheErrorHandler
	 */
	@Override
	public CacheErrorHandler errorHandler() {
		return cacheErrorHandler();
	}

	/**
	 * Creates the primary {@link RedisCacheManager} configured with Jackson 3 JSON serialization,
	 * default TTL, and custom per-cache expiration times.
	 *
	 * @param connectionFactory the Redis connection factory
	 * @param properties custom cache configuration properties
	 * @return fully configured RedisCacheManager
	 */
	@Bean
	@ConditionalOnMissingBean
	public RedisCacheManager redisCacheManager(
			RedisConnectionFactory connectionFactory,
			RedisCacheProperties properties) {

		var jsonSerializer = JacksonRedisSerializerFactory.createJsonRedisSerializer();
		var stringSerializer = new StringRedisSerializer();

		RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(properties.getDefaultTtl())
				.computePrefixWith(cacheName -> properties.getKeyPrefix() + cacheName + ":")
				.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

		if (!properties.isCacheNullValues()) {
			defaultConfig = defaultConfig.disableCachingNullValues();
		}

		Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
		if (properties.getExpires() != null) {
			for (var entry : properties.getExpires().entrySet()) {
				cacheConfigs.put(entry.getKey(), defaultConfig.entryTtl(entry.getValue()));
			}
		}

		return RedisCacheManager.builder(connectionFactory)
				.cacheDefaults(defaultConfig)
				.withInitialCacheConfigurations(cacheConfigs)
				.build();
	}

	/**
	 * Creates a {@link RedisTemplate} for general-purpose Redis operations using Jackson 3 JSON value serialization.
	 *
	 * @param connectionFactory the Redis connection factory
	 * @return configured RedisTemplate
	 */
	@Bean
	@ConditionalOnMissingBean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(JacksonRedisSerializerFactory.createJsonRedisSerializer());
		template.setHashValueSerializer(JacksonRedisSerializerFactory.createJsonRedisSerializer());
		template.afterPropertiesSet();
		return template;
	}

	/**
	 * Creates the {@link DistributedLockPort} bean backed by Redis atomic commands.
	 *
	 * @param stringRedisTemplate the StringRedisTemplate for lock state
	 * @return DistributedLockPort implementation
	 */
	@Bean
	@ConditionalOnMissingBean
	public DistributedLockPort distributedLockPort(StringRedisTemplate stringRedisTemplate) {
		return new RedisDistributedLockAdapter(stringRedisTemplate);
	}
}
