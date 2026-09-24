package io.github.edmaputra.uwati;

import org.springframework.boot.SpringApplication;

import io.github.edmaputra.uwati.bootstrap.UwatiApplication;

/**
 * Bootstrap entry point for running Uwati with Testcontainers backing infrastructure.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public class TestUwatiApplication {

	public static void main(String[] args) {
		SpringApplication.from(UwatiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
