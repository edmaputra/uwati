package io.github.edmaputra.uwati;

import org.springframework.boot.SpringApplication;

import io.github.edmaputra.uwati.bootstrap.UwatiApplication;

public class TestUwatiApplication {

	public static void main(String[] args) {
		SpringApplication.from(UwatiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
