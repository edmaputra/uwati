package io.github.edmaputra.uwati.cache.error;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests verifying that ResilienceCacheErrorHandler suppresses cache runtime exceptions.
 */
class ResilienceCacheErrorHandlerTest {

	private final ResilienceCacheErrorHandler errorHandler = new ResilienceCacheErrorHandler();

	@Test
	@DisplayName("Should handle cache get error silently without rethrowing")
	void shouldHandleCacheGetError() {
		Cache cache = mock(Cache.class);
		when(cache.getName()).thenReturn("test_cache");

		assertThatNoException().isThrownBy(() ->
				errorHandler.handleCacheGetError(new RuntimeException("Redis connection timeout"), cache, "test_key")
		);
	}

	@Test
	@DisplayName("Should handle cache put error silently without rethrowing")
	void shouldHandleCachePutError() {
		Cache cache = mock(Cache.class);
		when(cache.getName()).thenReturn("test_cache");

		assertThatNoException().isThrownBy(() ->
				errorHandler.handleCachePutError(new RuntimeException("Redis connection timeout"), cache, "test_key", "test_value")
		);
	}

	@Test
	@DisplayName("Should handle cache evict error silently without rethrowing")
	void shouldHandleCacheEvictError() {
		Cache cache = mock(Cache.class);
		when(cache.getName()).thenReturn("test_cache");

		assertThatNoException().isThrownBy(() ->
				errorHandler.handleCacheEvictError(new RuntimeException("Redis connection timeout"), cache, "test_key")
		);
	}

	@Test
	@DisplayName("Should handle cache clear error silently without rethrowing")
	void shouldHandleCacheClearError() {
		Cache cache = mock(Cache.class);
		when(cache.getName()).thenReturn("test_cache");

		assertThatNoException().isThrownBy(() ->
				errorHandler.handleCacheClearError(new RuntimeException("Redis connection timeout"), cache)
		);
	}
}
