package io.github.edmaputra.uwati.domain.organization.event;

import java.time.Instant;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.organization.Facility;
import io.github.edmaputra.uwati.domain.organization.FacilityStatus;

/**
 * Domain event published when the operational status of a {@link Facility} is altered.
 * <p>
 * Emitted by the core domain layer in the Hexagonal Architecture when a facility transitions
 * between active and inactive states.
 *
 * @param facility the facility whose status changed
 * @param oldStatus the preceding facility status
 * @param newStatus the new facility status
 * @param actor the identifier of the actor who initiated the status change
 * @param correlationId an optional correlation ID for request tracing across boundaries
 * @param occurredAt the timestamp when the status change occurred
 * @author edmaputra
 * @since 0.0.1
 */
public record FacilityStatusChanged(
		Facility facility,
		FacilityStatus oldStatus,
		FacilityStatus newStatus,
		String actor,
		String correlationId,
		Instant occurredAt) {

	/**
	 * Compact constructor validating invariant constraints for the facility status changed event.
	 *
	 * @param facility the facility
	 * @param oldStatus the previous status
	 * @param newStatus the updated status
	 * @param actor the initiating actor
	 * @param correlationId the correlation ID
	 * @param occurredAt the event timestamp
	 * @throws NullPointerException if any required parameter is null
	 */
	public FacilityStatusChanged {
		Objects.requireNonNull(facility, "Facility must not be null.");
		Objects.requireNonNull(oldStatus, "Old facility status must not be null.");
		Objects.requireNonNull(newStatus, "New facility status must not be null.");
		Objects.requireNonNull(actor, "Actor must not be null.");
		Objects.requireNonNull(occurredAt, "Occurred-at timestamp must not be null.");
	}

	/**
	 * Factory method creating a {@link FacilityStatusChanged} event with the current timestamp.
	 *
	 * @param facility the facility
	 * @param oldStatus the previous status
	 * @param newStatus the updated status
	 * @param actor the initiating actor
	 * @param correlationId an optional correlation ID for request tracing
	 * @return a new {@code FacilityStatusChanged} instance
	 */
	public static FacilityStatusChanged of(
			Facility facility,
			FacilityStatus oldStatus,
			FacilityStatus newStatus,
			String actor,
			String correlationId) {
		return new FacilityStatusChanged(facility, oldStatus, newStatus, actor, correlationId, Instant.now());
	}
}
