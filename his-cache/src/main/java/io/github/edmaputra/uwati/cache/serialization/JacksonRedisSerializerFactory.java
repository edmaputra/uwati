package io.github.edmaputra.uwati.cache.serialization;

import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

/**
 * Factory for creating Jackson 3 JSON serializers for Spring Data Redis.
 * <p>
 * Configures {@link GenericJacksonJsonRedisSerializer} with polymorphic type validation
 * and Spring Cache null-value support.
 */
public final class JacksonRedisSerializerFactory {

	private JacksonRedisSerializerFactory() {
	}

	/**
	 * Creates a {@link RedisSerializer} instance supporting JSON serialization of arbitrary objects,
	 * Java records, and null values.
	 *
	 * @return configured GenericJacksonJsonRedisSerializer
	 */
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
