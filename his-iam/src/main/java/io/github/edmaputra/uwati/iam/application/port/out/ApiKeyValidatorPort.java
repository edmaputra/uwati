package io.github.edmaputra.uwati.iam.application.port.out;

import java.util.Optional;

import io.github.edmaputra.uwati.iam.domain.auth.AuthenticatedIdentity;

/**
 * Outbound port for verifying machine-to-machine (M2M) API keys.
 */
public interface ApiKeyValidatorPort {

	/**
	 * Validates an API key and returns the associated identity if valid.
	 *
	 * @param apiKey the raw API key token string
	 * @return optional containing the identity if valid and active
	 */
	Optional<AuthenticatedIdentity> validateApiKey(String apiKey);
}
