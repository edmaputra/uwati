package io.github.edmaputra.uwati.iam.domain.model;

/**
 * Enumeration of account lifecycle statuses in the IAM subsystem.
 */
public enum UserStatus {
	/** Active account permitted to authenticate and access authorized resources. */
	ACTIVE,
	/** Temporarily suspended account prevented from logging in. */
	SUSPENDED,
	/** Deactivated/terminated account blocked from system access. */
	DEACTIVATED
}
