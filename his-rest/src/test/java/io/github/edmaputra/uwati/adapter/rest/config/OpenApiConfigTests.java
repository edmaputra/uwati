package io.github.edmaputra.uwati.adapter.rest.config;

import java.time.Instant;
import java.util.Optional;
import java.util.Properties;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link OpenApiConfig}.
 *
 * @author edmaputra
 * @since 0.0.1
 */
class OpenApiConfigTests {

	private final OpenApiConfig config = new OpenApiConfig();

	@Test
	void shouldUseDevVersionWhenBuildPropertiesAbsent() {
		OpenAPI openAPI = config.customOpenAPI(Optional.empty());

		assertThat(openAPI.getInfo()).isNotNull();
		assertThat(openAPI.getInfo().getTitle()).isEqualTo("Uwati HIS API");
		assertThat(openAPI.getInfo().getVersion()).isEqualTo("dev");
	}

	@Test
	void shouldReflectBuildPropertiesVersionWhenPresent() {
		Properties properties = new Properties();
		properties.setProperty("version", "1.2.3");
		properties.setProperty("name", "uwati");
		properties.setProperty("time", Instant.now().toString());
		BuildProperties buildProperties = new BuildProperties(properties);

		OpenAPI openAPI = config.customOpenAPI(Optional.of(buildProperties));

		assertThat(openAPI.getInfo()).isNotNull();
		assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.2.3");
	}
}
