package io.github.edmaputra.uwati.iam.domain.auth;

/**
 * Marker interface for strongly-typed authentication credentials accepted by {@link io.github.edmaputra.uwati.iam.application.port.out.AuthenticationProvider}s.
 */
public interface AuthCredentials {

	/**
	 * Returns the type of credential represented by this instance.
	 *
	 * @return the {@link AuthCredentialType}
	 */
	AuthCredentialType credentialType();
}
