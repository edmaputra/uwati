package io.github.edmaputra.uwati.cache.key;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import io.github.edmaputra.uwati.domain.tenancy.application.TenantContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;

/**
 * Spring Cache {@link KeyGenerator} that partitions cache keys by tenant ID to prevent cross-tenant leakage.
 * <p>
 * Extracts {@link TenantId} from method arguments or falls back to the current {@link TenantContext}.
 * Formats keys as {@code tenant:<tenantId>:<subKey>} or {@code global:<subKey>} when no tenant is active.
 */
@Component("tenantAwareKeyGenerator")
public class TenantAwareCacheKeyGenerator implements KeyGenerator {

	private final TenantContext tenantContext;

	/**
	 * Constructs the key generator with an optional tenant context provider.
	 *
	 * @param tenantContext optional tenant context
	 */
	public TenantAwareCacheKeyGenerator(Optional<TenantContext> tenantContext) {
		this.tenantContext = tenantContext.orElse(null);
	}

	@Override
	public Object generate(Object target, Method method, Object... params) {
		Optional<TenantId> tenantIdOpt = extractTenantId(params);

		String paramString = Arrays.stream(params)
				.filter(p -> !(p instanceof TenantId))
				.map(Objects::toString)
				.collect(Collectors.joining(":"));

		if (paramString.isEmpty()) {
			paramString = method.getName();
		}

		if (tenantIdOpt.isPresent()) {
			return formatTenantKey(tenantIdOpt.get(), paramString);
		}

		return formatGlobalKey(paramString);
	}

	/**
	 * Formats a key prefixed with the tenant ID namespace.
	 *
	 * @param tenantId the tenant ID
	 * @param subKey the specific cache key or query identifier
	 * @return formatted tenant key string ({@code tenant:<tenantId>:<subKey>})
	 */
	public static String formatTenantKey(TenantId tenantId, String subKey) {
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		return "tenant:" + tenantId.value() + ":" + subKey;
	}

	/**
	 * Formats a global key without a tenant namespace.
	 *
	 * @param subKey the specific cache key or query identifier
	 * @return formatted global key string ({@code global:<subKey>})
	 */
	public static String formatGlobalKey(String subKey) {
		return "global:" + subKey;
	}

	private Optional<TenantId> extractTenantId(Object[] params) {
		for (Object param : params) {
			if (param instanceof TenantId tenantId) {
				return Optional.of(tenantId);
			}
		}

		if (tenantContext != null) {
			return tenantContext.currentTenantId();
		}

		return Optional.empty();
	}
}
