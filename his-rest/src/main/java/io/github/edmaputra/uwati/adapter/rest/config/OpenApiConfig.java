package io.github.edmaputra.uwati.adapter.rest.config;

import java.util.Optional;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI and Swagger documentation configuration for Uwati HIS.
 * <p>
 * Automatically reflects the artifact build version when available,
 * falling back to {@code "dev"} in development environments.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI customOpenAPI(Optional<BuildProperties> buildProperties) {
		String version = buildProperties.map(BuildProperties::getVersion).orElse("dev");
		return new OpenAPI()
				.info(new Info()
						.title("Uwati HIS API")
						.description("Multi-tenant Hospital Information System REST API")
						.version(version));
	}
}
