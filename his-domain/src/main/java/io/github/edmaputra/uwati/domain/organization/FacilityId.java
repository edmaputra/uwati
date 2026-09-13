package io.github.edmaputra.uwati.domain.organization;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.util.UuidV7;

/**
 * Strongly typed identifier for a healthcare facility in the organization domain model.
 * <p>
 * Represents an immutable value object serving as the primary identity of a {@link Facility}
 * within the core domain layer of the Hexagonal Architecture.
 *
 * @param value the underlying UUID representing the facility identifier
 * @author edmaputra
 * @since 0.0.1
 */
public record FacilityId(UUID value) {

	/**
	 * Compact constructor validating that the facility ID value is not null.
	 *
	 * @param value the underlying UUID
	 * @throws NullPointerException if {@code value} is null
	 */
	public FacilityId {
		Objects.requireNonNull(value, "Facility ID must not be null.");
	}

	/**
	 * Generates a new unique {@link FacilityId} using UUID version 7.
	 *
	 * @return a new uniquely generated facility identifier
	 */
	public static FacilityId generate() {
		return new FacilityId(UuidV7.generate());
	}

	/**
	 * Creates a {@link FacilityId} from its string representation.
	 *
	 * @param value the string representation of the UUID
	 * @return the parsed facility identifier
	 * @throws IllegalArgumentException if {@code value} is null, blank, or not a valid UUID format
	 */
	public static FacilityId from(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Facility ID must not be blank.");
		}
		return new FacilityId(UUID.fromString(value));
	}

	/**
	 * Returns the string representation of the facility identifier.
	 *
	 * @return the UUID as a formatted string
	 */
	@Override
	public String toString() {
		return value.toString();
	}
}
