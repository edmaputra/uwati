package io.github.edmaputra.uwati.domain.organization;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.util.UuidV7;

/**
 * Strongly typed identifier for a service unit within a healthcare facility.
 * <p>
 * Represents an immutable value object serving as the primary identity of a {@link ServiceUnit}
 * within the core domain layer of the Hexagonal Architecture.
 *
 * @param value the underlying UUID representing the service unit identifier
 * @author edmaputra
 * @since 0.0.1
 */
public record ServiceUnitId(UUID value) {

	/**
	 * Compact constructor validating that the service unit ID value is not null.
	 *
	 * @param value the underlying UUID
	 * @throws NullPointerException if {@code value} is null
	 */
	public ServiceUnitId {
		Objects.requireNonNull(value, "Service Unit ID must not be null.");
	}

	/**
	 * Generates a new unique {@link ServiceUnitId} using UUID version 7.
	 *
	 * @return a new uniquely generated service unit identifier
	 */
	public static ServiceUnitId generate() {
		return new ServiceUnitId(UuidV7.generate());
	}

	/**
	 * Creates a {@link ServiceUnitId} from its string representation.
	 *
	 * @param value the string representation of the UUID
	 * @return the parsed service unit identifier
	 * @throws IllegalArgumentException if {@code value} is null, blank, or not a valid UUID format
	 */
	public static ServiceUnitId from(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Service Unit ID must not be blank.");
		}
		return new ServiceUnitId(UUID.fromString(value));
	}

	/**
	 * Returns the string representation of the service unit identifier.
	 *
	 * @return the UUID as a formatted string
	 */
	@Override
	public String toString() {
		return value.toString();
	}
}
