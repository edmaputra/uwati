package io.github.edmaputra.uwati.domain.organization.event;

import java.time.Instant;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.organization.ServiceUnit;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitStatus;

/**
 * Domain event published when the operational status of a {@link ServiceUnit} is changed.
 * <p>
 * Emitted by the core domain layer in the Hexagonal Architecture when a service unit transitions
 * between active and inactive states.
 *
 * @param serviceUnit the service unit whose status changed
 * @param oldStatus the preceding service unit status
 * @param newStatus the updated service unit status
 * @param actor the identifier of the actor who performed the status change
 * @param correlationId an optional correlation ID for request tracing across boundaries
 * @param occurredAt the timestamp when the status change occurred
 * @author edmaputra
 * @since 0.0.1
 */
public record ServiceUnitStatusChanged(
		ServiceUnit serviceUnit,
		ServiceUnitStatus oldStatus,
		ServiceUnitStatus newStatus,
		String actor,
		String correlationId,
		Instant occurredAt) {

	/**
	 * Compact constructor validating invariant constraints for the service unit status changed event.
	 *
	 * @param serviceUnit the service unit
	 * @param oldStatus the previous status
	 * @param newStatus the updated status
	 * @param actor the initiating actor
	 * @param correlationId the correlation ID
	 * @param occurredAt the event timestamp
	 * @throws NullPointerException if any required parameter is null
	 */
	public ServiceUnitStatusChanged {
		Objects.requireNonNull(serviceUnit, "Service unit must not be null.");
		Objects.requireNonNull(oldStatus, "Old service unit status must not be null.");
		Objects.requireNonNull(newStatus, "New service unit status must not be null.");
		Objects.requireNonNull(actor, "Actor must not be null.");
		Objects.requireNonNull(occurredAt, "Occurred-at timestamp must not be null.");
	}

	/**
	 * Factory method creating a {@link ServiceUnitStatusChanged} event with the current timestamp.
	 *
	 * @param serviceUnit the service unit
	 * @param oldStatus the previous status
	 * @param newStatus the updated status
	 * @param actor the initiating actor
	 * @param correlationId an optional correlation ID for request tracing
	 * @return a new {@code ServiceUnitStatusChanged} instance
	 */
	public static ServiceUnitStatusChanged of(
			ServiceUnit serviceUnit,
			ServiceUnitStatus oldStatus,
			ServiceUnitStatus newStatus,
			String actor,
			String correlationId) {
		return new ServiceUnitStatusChanged(serviceUnit, oldStatus, newStatus, actor, correlationId, Instant.now());
	}
}
