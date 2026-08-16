package io.github.edmaputra.uwati.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "io.github.edmaputra.uwati")
public class UwatiApplication {

	public static void main(String[] args) {
		SpringApplication.run(UwatiApplication.class, args);
	}

}
