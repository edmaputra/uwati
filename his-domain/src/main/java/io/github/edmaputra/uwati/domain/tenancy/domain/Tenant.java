package io.github.edmaputra.uwati.domain.tenancy.domain;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.audit.Auditable;

/**
 * Root aggregate entity representing a tenant (client organization) in the system.
 * <p>
 * Manages tenant lifecycle, names, and operational status in the core domain layer
 * of the Hexagonal Architecture. Implements {@link Auditable} to participate in audit logging.
 *
 * @param id the unique identifier of the tenant
 * @param legalName the registered legal name of the tenant organization
 * @param displayName the human-friendly display name of the tenant
 * @param status the current lifecycle status of the tenant
 * @param createdAt the timestamp when the tenant was created
 * @param updatedAt the timestamp when the tenant was last updated
 * @author edmaputra
 * @since 0.0.1
 */
public record Tenant(
		TenantId id,
		String legalName,
		String displayName,
		TenantStatus status,
		Instant createdAt,
		Instant updatedAt) implements Auditable {

	/**
	 * Compact constructor validating invariant constraints of a tenant entity.
	 *
	 * @param id the tenant ID
	 * @param legalName the legal name
	 * @param displayName the display name
	 * @param status the tenant status
	 * @param createdAt the creation timestamp
	 * @param updatedAt the last update timestamp
	 * @throws NullPointerException if {@code id}, {@code status}, {@code createdAt}, or {@code updatedAt} is null
	 * @throws IllegalArgumentException if {@code legalName} or {@code displayName} is null or blank,
	 *                                  or if {@code updatedAt} is before {@code createdAt}
	 */
	public Tenant {
		Objects.requireNonNull(id, "Tenant ID must not be null.");
		if (legalName == null || legalName.isBlank()) {
			throw new IllegalArgumentException("Tenant legal name must not be blank.");
		}
		if (displayName == null || displayName.isBlank()) {
			throw new IllegalArgumentException("Tenant display name must not be blank.");
		}
		Objects.requireNonNull(status, "Tenant status must not be null.");
		Objects.requireNonNull(createdAt, "Tenant creation timestamp must not be null.");
		Objects.requireNonNull(updatedAt, "Tenant update timestamp must not be null.");
		if (updatedAt.isBefore(createdAt)) {
			throw new IllegalArgumentException("Tenant update timestamp must not precede its creation timestamp.");
		}
	}

	/**
	 * Checks whether the tenant is currently active.
	 *
	 * @return {@code true} if {@link #status()} equals {@link TenantStatus#ACTIVE}, {@code false} otherwise
	 */
	public boolean isActive() {
		return status == TenantStatus.ACTIVE;
	}

	/**
	 * Returns the map of fields and values that are subject to audit logging.
	 *
	 * @return an unmodifiable map containing the tenant's auditable attributes
	 */
	@Override
	public Map<String, Object> auditableFields() {
		return Map.of(
				"displayName", displayName,
				"legalName", legalName,
				"status", status.name());
	}
}
