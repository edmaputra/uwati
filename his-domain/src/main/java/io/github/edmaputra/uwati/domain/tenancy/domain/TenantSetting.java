package io.github.edmaputra.uwati.domain.tenancy.domain;

import java.util.Map;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.audit.Auditable;

/**
 * Domain entity representing an individual tenant configuration key-value setting.
 * <p>
 * Maintains versioned configuration attributes such as timezones, locales, and feature flags
 * within the core domain layer of the Hexagonal Architecture. Implements {@link Auditable}
 * to support audit trail tracking.
 *
 * @param tenantId the identifier of the tenant that owns this setting
 * @param key the configuration key identifier
 * @param value the configuration value
 * @param revision the optimistic concurrency revision counter, must be at least 1
 * @author edmaputra
 * @since 0.0.1
 */
public record TenantSetting(
		TenantId tenantId,
		String key,
		String value,
		int revision) implements Auditable {

	/**
	 * Compact constructor validating invariant constraints for a tenant setting.
	 *
	 * @param tenantId the identifier of the owning tenant
	 * @param key the configuration key
	 * @param value the configuration value
	 * @param revision the revision counter
	 * @throws NullPointerException if {@code tenantId} is null
	 * @throws IllegalArgumentException if {@code key} or {@code value} is null or blank, or if {@code revision} is less than 1
	 */
	public TenantSetting {
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		if (key == null || key.isBlank()) {
			throw new IllegalArgumentException("Setting key must not be blank.");
		}
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Setting value must not be blank.");
		}
		if (revision < 1) {
			throw new IllegalArgumentException("Revision must be greater than or equal to 1.");
		}
	}

	/**
	 * Creates a new {@link TenantSetting} copy with the specified value and an incremented revision number.
	 *
	 * @param newValue the updated setting value
	 * @return a new setting instance with updated value and revision incremented by one
	 */
	public TenantSetting withIncrementedRevision(String newValue) {
		return new TenantSetting(tenantId, key, newValue, revision + 1);
	}

	/**
	 * Returns the map of fields and values that are subject to audit logging.
	 *
	 * @return an unmodifiable map containing the setting's auditable attributes
	 */
	@Override
	public Map<String, Object> auditableFields() {
		return Map.of(
				"value", value,
				"revision", revision);
	}
}
