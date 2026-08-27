package io.github.edmaputra.uwati.iam.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import lombok.Getter;

@Getter
public class UserIdentity {

	private final UserIdentityId id;
	private final UserId userId;
	private final ProviderType providerType;
	private final String externalSubjectId;
	private final String issuerUrl;
	private final Instant createdAt;

	public UserIdentity(
			UserIdentityId id,
			UserId userId,
			ProviderType providerType,
			String externalSubjectId,
			String issuerUrl,
			Instant createdAt) {
		this.id = Objects.requireNonNull(id, "UserIdentity ID must not be null.");
		this.userId = Objects.requireNonNull(userId, "User ID must not be null.");
		this.providerType = Objects.requireNonNull(providerType, "ProviderType must not be null.");
		this.externalSubjectId = Objects.requireNonNull(externalSubjectId, "External subject ID must not be null.");
		this.issuerUrl = issuerUrl;
		this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt must not be null.");
	}

	public static UserIdentity create(
			UserId userId,
			ProviderType providerType,
			String externalSubjectId,
			String issuerUrl) {
		return new UserIdentity(
				UserIdentityId.generate(),
				userId,
				providerType,
				externalSubjectId,
				issuerUrl,
				Instant.now());
	}

	public Optional<String> optionalIssuerUrl() {
		return Optional.ofNullable(issuerUrl);
	}
}
