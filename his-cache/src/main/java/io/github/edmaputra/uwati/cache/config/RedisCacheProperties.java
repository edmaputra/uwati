package io.github.edmaputra.uwati.cache.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "uwati.cache")
public class RedisCacheProperties {

	private boolean enabled = true;

	private String keyPrefix = "uwati:";

	private Duration defaultTtl = Duration.ofMinutes(30);

	private boolean cacheNullValues = true;

	private Duration nullTtl = Duration.ofMinutes(2);

	private Map<String, Duration> expires = new HashMap<>(Map.of(
			"tenant_settings", Duration.ofHours(1),
			"tenants", Duration.ofMinutes(30),
			"scopes", Duration.ofMinutes(30),
			"effective_access", Duration.ofMinutes(15),
			"token_blacklist", Duration.ofMinutes(15)));
}
