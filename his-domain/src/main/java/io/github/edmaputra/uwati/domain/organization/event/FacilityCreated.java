package io.github.edmaputra.uwati.domain.organization.event;

import java.time.Instant;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.organization.Facility;

/**
 * Domain event published when a new {@link Facility} is successfully registered.
 * <p>
 * Emitted by the core domain layer in the Hexagonal Architecture to notify other
 * modules and downstream adapters of facility creation.
 *
 * @param facility the newly created facility aggregate
 * @param actor the identifier of the actor who performed the creation
 * @param correlationId an optional correlation ID for request tracing across boundaries
 * @param occurredAt the timestamp when the event occurred
 * @author edmaputra
 * @since 0.0.1
 */
public record FacilityCreated(Facility facility, String actor, String correlationId, Instant occurredAt) {

	/**
	 * Compact constructor validating invariant constraints for the facility created event.
	 *
	 * @param facility the created facility
	 * @param actor the initiating actor
	 * @param correlationId the correlation ID
	 * @param occurredAt the event timestamp
	 * @throws NullPointerException if {@code facility}, {@code actor}, or {@code occurredAt} is null
	 */
	public FacilityCreated {
		Objects.requireNonNull(facility, "Facility must not be null.");
		Objects.requireNonNull(actor, "Actor must not be null.");
		Objects.requireNonNull(occurredAt, "Occurred-at timestamp must not be null.");
	}

	/**
	 * Factory method creating a {@link FacilityCreated} event with the current timestamp.
	 *
	 * @param facility the created facility
	 * @param actor the initiating actor
	 * @param correlationId an optional correlation ID for request tracing
	 * @return a new {@code FacilityCreated} instance
	 */
	public static FacilityCreated of(Facility facility, String actor, String correlationId) {
		return new FacilityCreated(facility, actor, correlationId, Instant.now());
	}
}
