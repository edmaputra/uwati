package io.github.edmaputra.uwati.cache.serialization;

import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JacksonRedisSerializerFactory {

	private JacksonRedisSerializerFactory() {
	}

	public static ObjectMapper createObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
				.allowIfSubType(Object.class)
				.build();

		objectMapper.activateDefaultTyping(
				typeValidator,
				ObjectMapper.DefaultTyping.EVERYTHING,
				JsonTypeInfo.As.PROPERTY);

		return objectMapper;
	}

	public static RedisSerializer<Object> createJsonRedisSerializer() {
		return new GenericJackson2JsonRedisSerializer(createObjectMapper());
	}
}
