package io.github.edmaputra.uwati;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import io.github.edmaputra.uwati.bootstrap.UwatiApplication;
import io.github.edmaputra.uwati.test.RequiresDocker;

@RequiresDocker
@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = UwatiApplication.class)
class UwatiApplicationTests {

	@Test
	void contextLoads() {
	}

}
