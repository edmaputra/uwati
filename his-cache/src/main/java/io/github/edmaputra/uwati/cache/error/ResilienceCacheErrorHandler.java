package io.github.edmaputra.uwati.cache.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/**
 * Resilient {@link CacheErrorHandler} that handles Redis/Valkey outages gracefully.
 * <p>
 * Positioned in the caching infrastructure layer of the hexagonal architecture, this error
 * handler ensures high availability. Rather than rethrowing exceptions and breaking
 * user-facing requests during cache downtime, it logs warnings and allows the application
 * to fall through transparently to primary database queries.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public class ResilienceCacheErrorHandler implements CacheErrorHandler {

	private static final Logger log = LoggerFactory.getLogger(ResilienceCacheErrorHandler.class);

	/**
	 * Handles errors during cache read operations by logging a warning and letting execution proceed to the database.
	 *
	 * @param exception the runtime exception thrown by the cache provider
	 * @param cache the cache instance where the error occurred
	 * @param key the cache key involved in the operation
	 */
	@Override
	public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
		log.warn("Redis cache GET failure for cache '{}' and key '{}'. Falling back to database.",
				cache != null ? cache.getName() : "unknown", key, exception);
	}

	/**
	 * Handles errors during cache write operations by logging a warning without failing the business operation.
	 *
	 * @param exception the runtime exception thrown by the cache provider
	 * @param cache the cache instance where the error occurred
	 * @param key the cache key involved in the operation
	 * @param value the value being cached
	 */
	@Override
	public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
		log.warn("Redis cache PUT failure for cache '{}' and key '{}'. Database operation succeeded.",
				cache != null ? cache.getName() : "unknown", key, exception);
	}

	/**
	 * Handles errors during cache eviction operations by logging a warning.
	 *
	 * @param exception the runtime exception thrown by the cache provider
	 * @param cache the cache instance where the error occurred
	 * @param key the cache key involved in the operation
	 */
	@Override
	public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
		log.warn("Redis cache EVICT failure for cache '{}' and key '{}'.",
				cache != null ? cache.getName() : "unknown", key, exception);
	}

	/**
	 * Handles errors during cache clear operations by logging a warning.
	 *
	 * @param exception the runtime exception thrown by the cache provider
	 * @param cache the cache instance where the error occurred
	 */
	@Override
	public void handleCacheClearError(RuntimeException exception, Cache cache) {
		log.warn("Redis cache CLEAR failure for cache '{}'.",
				cache != null ? cache.getName() : "unknown", exception);
	}
}
