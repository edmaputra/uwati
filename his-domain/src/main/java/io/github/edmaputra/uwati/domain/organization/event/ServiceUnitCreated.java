package io.github.edmaputra.uwati.domain.organization.event;

import java.time.Instant;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.organization.ServiceUnit;

/**
 * Domain event published when a new {@link ServiceUnit} is registered within a facility.
 * <p>
 * Emitted by the core domain layer in the Hexagonal Architecture to notify other
 * modules and downstream adapters of service unit creation.
 *
 * @param serviceUnit the newly created service unit aggregate
 * @param actor the identifier of the actor who performed the creation
 * @param correlationId an optional correlation ID for request tracing across boundaries
 * @param occurredAt the timestamp when the event occurred
 * @author edmaputra
 * @since 0.0.1
 */
public record ServiceUnitCreated(
		ServiceUnit serviceUnit,
		String actor,
		String correlationId,
		Instant occurredAt) {

	/**
	 * Compact constructor validating invariant constraints for the service unit created event.
	 *
	 * @param serviceUnit the created service unit
	 * @param actor the initiating actor
	 * @param correlationId the correlation ID
	 * @param occurredAt the event timestamp
	 * @throws NullPointerException if any required parameter is null
	 */
	public ServiceUnitCreated {
		Objects.requireNonNull(serviceUnit, "Service unit must not be null.");
		Objects.requireNonNull(actor, "Actor must not be null.");
		Objects.requireNonNull(occurredAt, "Occurred-at timestamp must not be null.");
	}

	/**
	 * Factory method creating a {@link ServiceUnitCreated} event with the current timestamp.
	 *
	 * @param serviceUnit the created service unit
	 * @param actor the initiating actor
	 * @param correlationId an optional correlation ID for request tracing
	 * @return a new {@code ServiceUnitCreated} instance
	 */
	public static ServiceUnitCreated of(ServiceUnit serviceUnit, String actor, String correlationId) {
		return new ServiceUnitCreated(serviceUnit, actor, correlationId, Instant.now());
	}
}
