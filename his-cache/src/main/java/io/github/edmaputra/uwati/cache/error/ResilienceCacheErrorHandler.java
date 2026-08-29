package io.github.edmaputra.uwati.cache.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

public class ResilienceCacheErrorHandler implements CacheErrorHandler {

	private static final Logger log = LoggerFactory.getLogger(ResilienceCacheErrorHandler.class);

	@Override
	public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
		log.warn("Redis cache GET failure for cache '{}' and key '{}'. Falling back to database.",
				cache != null ? cache.getName() : "unknown", key, exception);
	}

	@Override
	public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
		log.warn("Redis cache PUT failure for cache '{}' and key '{}'. Database operation succeeded.",
				cache != null ? cache.getName() : "unknown", key, exception);
	}

	@Override
	public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
		log.warn("Redis cache EVICT failure for cache '{}' and key '{}'.",
				cache != null ? cache.getName() : "unknown", key, exception);
	}

	@Override
	public void handleCacheClearError(RuntimeException exception, Cache cache) {
		log.warn("Redis cache CLEAR failure for cache '{}'.",
				cache != null ? cache.getName() : "unknown", exception);
	}
}
