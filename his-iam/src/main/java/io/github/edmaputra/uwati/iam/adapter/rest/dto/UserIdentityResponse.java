package io.github.edmaputra.uwati.iam.adapter.rest.dto;

import java.time.Instant;

import io.github.edmaputra.uwati.iam.domain.model.UserIdentity;

/**
 * REST response representing a federated SSO identity linked to a user.
 *
 * @param id                the unique identity linkage ID
 * @param providerType      the identity provider type
 * @param externalSubjectId the external subject ID claim
 * @param issuerUrl         the issuer URL
 * @param createdAt         creation timestamp
 * @author edmaputra
 */
public record UserIdentityResponse(
		String id,
		String providerType,
		String externalSubjectId,
		String issuerUrl,
		Instant createdAt) {

	/**
	 * Maps a domain {@link UserIdentity} to a REST response DTO.
	 *
	 * @param identity the domain identity
	 * @return the response DTO
	 */
	public static UserIdentityResponse from(UserIdentity identity) {
		return new UserIdentityResponse(
				identity.getId().value().toString(),
				identity.getProviderType().name(),
				identity.getExternalSubjectId(),
				identity.optionalIssuerUrl().orElse(null),
				identity.getCreatedAt());
	}
}
