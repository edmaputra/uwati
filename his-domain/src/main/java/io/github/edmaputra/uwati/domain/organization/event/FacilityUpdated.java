package io.github.edmaputra.uwati.domain.organization.event;

import java.time.Instant;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.organization.Facility;

/**
 * Domain event published when the profile or configuration of an existing {@link Facility} is updated.
 * <p>
 * Emitted by the core domain layer in the Hexagonal Architecture containing the previous and current
 * snapshots of the facility aggregate.
 *
 * @param previous the state of the facility before the update
 * @param current the state of the facility after the update
 * @param actor the identifier of the actor who performed the update
 * @param correlationId an optional correlation ID for request tracing across boundaries
 * @param occurredAt the timestamp when the update occurred
 * @author edmaputra
 * @since 0.0.1
 */
public record FacilityUpdated(
		Facility previous,
		Facility current,
		String actor,
		String correlationId,
		Instant occurredAt) {

	/**
	 * Compact constructor validating invariant constraints for the facility updated event.
	 *
	 * @param previous the previous facility state
	 * @param current the current facility state
	 * @param actor the initiating actor
	 * @param correlationId the correlation ID
	 * @param occurredAt the event timestamp
	 * @throws NullPointerException if any required parameter is null
	 */
	public FacilityUpdated {
		Objects.requireNonNull(previous, "Previous facility must not be null.");
		Objects.requireNonNull(current, "Current facility must not be null.");
		Objects.requireNonNull(actor, "Actor must not be null.");
		Objects.requireNonNull(occurredAt, "Occurred-at timestamp must not be null.");
	}

	/**
	 * Factory method creating a {@link FacilityUpdated} event with the current timestamp.
	 *
	 * @param previous the previous facility state
	 * @param current the current facility state
	 * @param actor the initiating actor
	 * @param correlationId an optional correlation ID for request tracing
	 * @return a new {@code FacilityUpdated} instance
	 */
	public static FacilityUpdated of(Facility previous, Facility current, String actor, String correlationId) {
		return new FacilityUpdated(previous, current, actor, correlationId, Instant.now());
	}
}
