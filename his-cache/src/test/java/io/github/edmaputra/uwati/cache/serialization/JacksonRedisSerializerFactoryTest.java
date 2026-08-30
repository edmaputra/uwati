package io.github.edmaputra.uwati.cache.serialization;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.RedisSerializer;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests verifying Jackson 3 Redis serialization and deserialization of domain records and arrays.
 */
class JacksonRedisSerializerFactoryTest {

	@Test
	@DisplayName("Should serialize and deserialize TenantSetting record correctly")
	void shouldSerializeAndDeserializeTenantSetting() {
		RedisSerializer<Object> serializer = JacksonRedisSerializerFactory.createJsonRedisSerializer();
		TenantId tenantId = new TenantId(UUID.randomUUID());
		TenantSetting original = new TenantSetting(tenantId, "TEST_KEY", "TEST_VAL", 1);

		byte[] raw = serializer.serialize(original);
		assertThat(raw).isNotEmpty();

		Object deserialized = serializer.deserialize(raw);
		assertThat(deserialized).isEqualTo(original);
	}

	@Test
	@DisplayName("Should serialize and deserialize Array of TenantSettings correctly")
	void shouldSerializeAndDeserializeArray() {
		RedisSerializer<Object> serializer = JacksonRedisSerializerFactory.createJsonRedisSerializer();
		TenantId tenantId = new TenantId(UUID.randomUUID());
		TenantSetting[] original = new TenantSetting[] {
				new TenantSetting(tenantId, "KEY1", "VAL1", 1),
				new TenantSetting(tenantId, "KEY2", "VAL2", 2)
		};

		byte[] raw = serializer.serialize(original);
		assertThat(raw).isNotEmpty();

		Object deserialized = serializer.deserialize(raw);
		assertThat(deserialized).isEqualTo(original);
	}
}
