package io.github.edmaputra.uwati.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot application entry point for Uwati Hospital Information System (HIS).
 * <p>
 * Configures component scanning, JPA entity scanning, and JPA repository management
 * across all modular boundaries of the application.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@SpringBootApplication(scanBasePackages = "io.github.edmaputra.uwati")
@EntityScan(basePackages = "io.github.edmaputra.uwati")
@EnableJpaRepositories(basePackages = "io.github.edmaputra.uwati.adapter.persistence")
public class UwatiApplication {

	/**
	 * Main entry point method used to bootstrap and launch the Spring Boot application.
	 *
	 * @param args command-line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(UwatiApplication.class, args);
	}

}
