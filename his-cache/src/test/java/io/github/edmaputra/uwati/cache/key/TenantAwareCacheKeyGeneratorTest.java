package io.github.edmaputra.uwati.cache.key;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.edmaputra.uwati.domain.tenancy.application.TenantContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TenantAwareCacheKeyGeneratorTest {

	@Test
	@DisplayName("Should extract TenantId from method arguments")
	void shouldExtractTenantIdFromArguments() throws NoSuchMethodException {
		TenantAwareCacheKeyGenerator generator = new TenantAwareCacheKeyGenerator(Optional.empty());
		Method method = DummyService.class.getMethod("findByKey", TenantId.class, String.class);

		TenantId tenantId = new TenantId(UUID.randomUUID());
		Object key = generator.generate(new DummyService(), method, tenantId, "SETTING_KEY");

		assertThat(key).isEqualTo("tenant:" + tenantId.value() + ":SETTING_KEY");
	}

	@Test
	@DisplayName("Should extract TenantId from TenantContext when not in arguments")
	void shouldExtractTenantIdFromContext() throws NoSuchMethodException {
		TenantId contextTenantId = new TenantId(UUID.randomUUID());
		TenantContext tenantContext = mock(TenantContext.class);
		when(tenantContext.currentTenantId()).thenReturn(Optional.of(contextTenantId));

		TenantAwareCacheKeyGenerator generator = new TenantAwareCacheKeyGenerator(Optional.of(tenantContext));
		Method method = DummyService.class.getMethod("findAll");

		Object key = generator.generate(new DummyService(), method);

		assertThat(key).isEqualTo("tenant:" + contextTenantId.value() + ":findAll");
	}

	@Test
	@DisplayName("Should generate global key when no TenantId is in arguments or context")
	void shouldGenerateGlobalKey() throws NoSuchMethodException {
		TenantAwareCacheKeyGenerator generator = new TenantAwareCacheKeyGenerator(Optional.empty());
		Method method = DummyService.class.getMethod("findAll");

		Object key = generator.generate(new DummyService(), method);

		assertThat(key).isEqualTo("global:findAll");
	}

	@Test
	@DisplayName("Should format tenant key correctly via static helper")
	void shouldFormatTenantKeyStaticHelper() {
		TenantId tenantId = new TenantId(UUID.randomUUID());
		String formatted = TenantAwareCacheKeyGenerator.formatTenantKey(tenantId, "all");

		assertThat(formatted).isEqualTo("tenant:" + tenantId.value() + ":all");
	}

	static class DummyService {
		public void findByKey(TenantId tenantId, String key) {}
		public void findAll() {}
	}
}
