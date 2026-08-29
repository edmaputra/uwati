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

@Component("tenantAwareKeyGenerator")
public class TenantAwareCacheKeyGenerator implements KeyGenerator {

	private final TenantContext tenantContext;

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

	public static String formatTenantKey(TenantId tenantId, String subKey) {
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		return "tenant:" + tenantId.value() + ":" + subKey;
	}

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
