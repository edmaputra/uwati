package io.github.edmaputra.uwati;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
			.withDatabaseName("uwati")
			.withUsername("uwati")
			.withPassword("uwati");

	private static final GenericContainer<?> REDIS = new GenericContainer<>("valkey/valkey:9.1.1-alpine3.24")
			.withExposedPorts(6379);

	@Bean
	@ServiceConnection
	public PostgreSQLContainer<?> postgresContainer() {
		return POSTGRES;
	}

	@Bean
	@ServiceConnection(name = "redis")
	public GenericContainer<?> redisContainer() {
		return REDIS;
	}

}
