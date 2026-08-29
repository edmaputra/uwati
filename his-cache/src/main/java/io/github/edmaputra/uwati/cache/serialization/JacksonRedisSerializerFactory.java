package io.github.edmaputra.uwati.cache.serialization;

import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

public final class JacksonRedisSerializerFactory {

	private JacksonRedisSerializerFactory() {
	}

	public static RedisSerializer<Object> createJsonRedisSerializer() {
		PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
				.allowIfSubType(Object.class)
				.build();

		return GenericJacksonJsonRedisSerializer.builder()
				.enableDefaultTyping(typeValidator)
				.enableSpringCacheNullValueSupport()
				.build();
	}
}
