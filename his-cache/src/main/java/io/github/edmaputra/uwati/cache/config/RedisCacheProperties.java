package io.github.edmaputra.uwati.cache.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties for distributed caching in Uwati HIS.
 * <p>
 * Configurable under the {@code uwati.cache} prefix in application configuration files.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "uwati.cache")
public class RedisCacheProperties {

	/**
	 * Whether distributed caching is enabled. Default is {@code true}.
	 */
	private boolean enabled = true;

	/**
	 * Global key prefix applied to all Redis cache keys. Default is {@code "uwati:"}.
	 */
	private String keyPrefix = "uwati:";

	/**
	 * Default time-to-live for cache entries when not explicitly configured in {@link #expires}. Default is 30 minutes.
	 */
	private Duration defaultTtl = Duration.ofMinutes(30);

	/**
	 * Whether {@code null} values should be cached to prevent cache penetration. Default is {@code true}.
	 */
	private boolean cacheNullValues = true;

	/**
	 * Custom TTL for caching {@code null} values. Default is 2 minutes.
	 */
	private Duration nullTtl = Duration.ofMinutes(2);

	/**
	 * Per-cache TTL overrides mapping cache names to their respective durations.
	 */
	private Map<String, Duration> expires = new HashMap<>(Map.of(
			"tenant_settings", Duration.ofHours(1),
			"tenants", Duration.ofMinutes(30),
			"scopes", Duration.ofMinutes(30),
			"effective_access", Duration.ofMinutes(15),
			"token_blacklist", Duration.ofMinutes(15)));
}
