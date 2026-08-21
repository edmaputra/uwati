package io.github.edmaputra.uwati.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "io.github.edmaputra.uwati")
@EntityScan(basePackages = "io.github.edmaputra.uwati")
@EnableJpaRepositories(basePackages = "io.github.edmaputra.uwati.adapter.persistence")
public class UwatiApplication {

	public static void main(String[] args) {
		SpringApplication.run(UwatiApplication.class, args);
	}

}
