package io.github.edmaputra.uwati.domain.tenancy.domain;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.util.UuidV7;

/**
 * Strongly typed identifier for a tenant in the domain model.
 * <p>
 * Represents an immutable value object serving as the primary identity of a {@link Tenant}
 * within the core domain layer of the Hexagonal Architecture.
 *
 * @param value the underlying UUID representing the tenant identifier
 * @author edmaputra
 * @since 0.0.1
 */
public record TenantId(UUID value) {

	/**
	 * Compact constructor validating that the tenant ID value is not null.
	 *
	 * @param value the underlying UUID
	 * @throws NullPointerException if {@code value} is null
	 */
	public TenantId {
		Objects.requireNonNull(value, "Tenant ID must not be null.");
	}

	/**
	 * Generates a new unique {@link TenantId} using UUID version 7.
	 *
	 * @return a new uniquely generated tenant identifier
	 */
	public static TenantId generate() {
		return new TenantId(UuidV7.generate());
	}

	/**
	 * Creates a {@link TenantId} from its string representation.
	 *
	 * @param value the string representation of the UUID
	 * @return the parsed tenant identifier
	 * @throws IllegalArgumentException if {@code value} is null, blank, or not a valid UUID format
	 */
	public static TenantId from(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Tenant ID must not be blank.");
		}
		return new TenantId(UUID.fromString(value));
	}

	/**
	 * Returns the string representation of the tenant identifier.
	 *
	 * @return the UUID as a formatted string
	 */
	@Override
	public String toString() {
		return value.toString();
	}
}
