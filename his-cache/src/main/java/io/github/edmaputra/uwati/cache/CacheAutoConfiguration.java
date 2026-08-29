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

@AutoConfiguration
@EnableCaching
@ConditionalOnClass(RedisConnectionFactory.class)
@ConditionalOnProperty(prefix = "uwati.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RedisCacheProperties.class)
public class CacheAutoConfiguration implements CachingConfigurer {

	@Bean
	@ConditionalOnMissingBean(name = "tenantAwareKeyGenerator")
	public KeyGenerator tenantAwareKeyGenerator(Optional<TenantContext> tenantContext) {
		return new TenantAwareCacheKeyGenerator(tenantContext);
	}

	@Bean
	@ConditionalOnMissingBean
	public CacheErrorHandler cacheErrorHandler() {
		return new ResilienceCacheErrorHandler();
	}

	@Override
	public CacheErrorHandler errorHandler() {
		return cacheErrorHandler();
	}

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

	@Bean
	@ConditionalOnMissingBean
	public DistributedLockPort distributedLockPort(StringRedisTemplate stringRedisTemplate) {
		return new RedisDistributedLockAdapter(stringRedisTemplate);
	}
}
