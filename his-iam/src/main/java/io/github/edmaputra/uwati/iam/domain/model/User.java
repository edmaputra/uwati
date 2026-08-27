package io.github.edmaputra.uwati.iam.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import lombok.Getter;

@Getter
public class User {

	private final UserId id;
	private final String email;
	private String passwordHash;
	private String fullName;
	private UserStatus status;
	private final boolean platformSuperAdmin;
	private final Instant createdAt;
	private Instant updatedAt;

	public User(
			UserId id,
			String email,
			String passwordHash,
			String fullName,
			UserStatus status,
			boolean platformSuperAdmin,
			Instant createdAt,
			Instant updatedAt) {
		this.id = Objects.requireNonNull(id, "User ID must not be null.");
		this.email = validateEmail(email);
		this.passwordHash = passwordHash;
		this.fullName = validateFullName(fullName);
		this.status = Objects.requireNonNull(status, "Status must not be null.");
		this.platformSuperAdmin = platformSuperAdmin;
		this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt must not be null.");
		this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt must not be null.");
	}

	public static User create(
			String email,
			String passwordHash,
			String fullName,
			boolean platformSuperAdmin) {
		Instant now = Instant.now();
		return new User(
				UserId.generate(),
				email,
				passwordHash,
				fullName,
				UserStatus.ACTIVE,
				platformSuperAdmin,
				now,
				now);
	}

	public static User createExternal(
			String email,
			String fullName,
			boolean platformSuperAdmin) {
		return create(email, null, fullName, platformSuperAdmin);
	}

	public void updateProfile(String fullName) {
		this.fullName = validateFullName(fullName);
		this.updatedAt = Instant.now();
	}

	public void updatePassword(String passwordHash) {
		this.passwordHash = Objects.requireNonNull(passwordHash, "Password hash must not be null.");
		this.updatedAt = Instant.now();
	}

	public void activate() {
		if (this.status == UserStatus.DEACTIVATED) {
			throw new IllegalStateException("Deactivated user cannot be directly activated.");
		}
		this.status = UserStatus.ACTIVE;
		this.updatedAt = Instant.now();
	}

	public void suspend() {
		if (this.status == UserStatus.DEACTIVATED) {
			throw new IllegalStateException("Deactivated user cannot be suspended.");
		}
		this.status = UserStatus.SUSPENDED;
		this.updatedAt = Instant.now();
	}

	public void deactivate() {
		this.status = UserStatus.DEACTIVATED;
		this.updatedAt = Instant.now();
	}

	public boolean isActive() {
		return this.status == UserStatus.ACTIVE;
	}

	public boolean isSuspended() {
		return this.status == UserStatus.SUSPENDED;
	}

	public boolean isDeactivated() {
		return this.status == UserStatus.DEACTIVATED;
	}

	public Optional<String> optionalPasswordHash() {
		return Optional.ofNullable(passwordHash);
	}

	private static String validateEmail(String email) {
		Objects.requireNonNull(email, "Email must not be null.");
		String trimmed = email.trim().toLowerCase();
		if (trimmed.isBlank() || !trimmed.contains("@")) {
			throw new IllegalArgumentException("Invalid email format: " + email);
		}
		return trimmed;
	}

	private static String validateFullName(String fullName) {
		Objects.requireNonNull(fullName, "Full name must not be null.");
		String trimmed = fullName.trim();
		if (trimmed.isBlank()) {
			throw new IllegalArgumentException("Full name must not be blank.");
		}
		return trimmed;
	}
}
