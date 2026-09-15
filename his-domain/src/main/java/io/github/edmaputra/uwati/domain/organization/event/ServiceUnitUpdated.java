package io.github.edmaputra.uwati.domain.organization.event;

import java.time.Instant;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.organization.ServiceUnit;

/**
 * Domain event published when the configuration or profile of a {@link ServiceUnit} is updated.
 * <p>
 * Emitted by the core domain layer in the Hexagonal Architecture containing the previous and current
 * snapshots of the service unit aggregate.
 *
 * @param previous the state of the service unit before the update
 * @param current the state of the service unit after the update
 * @param actor the identifier of the actor who performed the update
 * @param correlationId an optional correlation ID for request tracing across boundaries
 * @param occurredAt the timestamp when the update occurred
 * @author edmaputra
 * @since 0.0.1
 */
public record ServiceUnitUpdated(
		ServiceUnit previous,
		ServiceUnit current,
		String actor,
		String correlationId,
		Instant occurredAt) {

	/**
	 * Compact constructor validating invariant constraints for the service unit updated event.
	 *
	 * @param previous the previous service unit state
	 * @param current the current service unit state
	 * @param actor the initiating actor
	 * @param correlationId the correlation ID
	 * @param occurredAt the event timestamp
	 * @throws NullPointerException if any required parameter is null
	 */
	public ServiceUnitUpdated {
		Objects.requireNonNull(previous, "Previous service unit must not be null.");
		Objects.requireNonNull(current, "Current service unit must not be null.");
		Objects.requireNonNull(actor, "Actor must not be null.");
		Objects.requireNonNull(occurredAt, "Occurred-at timestamp must not be null.");
	}

	/**
	 * Factory method creating a {@link ServiceUnitUpdated} event with the current timestamp.
	 *
	 * @param previous the previous service unit state
	 * @param current the current service unit state
	 * @param actor the initiating actor
	 * @param correlationId an optional correlation ID for request tracing
	 * @return a new {@code ServiceUnitUpdated} instance
	 */
	public static ServiceUnitUpdated of(
			ServiceUnit previous,
			ServiceUnit current,
			String actor,
			String correlationId) {
		return new ServiceUnitUpdated(previous, current, actor, correlationId, Instant.now());
	}
}
