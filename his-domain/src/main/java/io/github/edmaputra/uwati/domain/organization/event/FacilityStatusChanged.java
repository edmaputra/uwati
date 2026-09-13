package io.github.edmaputra.uwati.domain.organization.event;

import java.time.Instant;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.organization.Facility;
import io.github.edmaputra.uwati.domain.organization.FacilityStatus;

public record FacilityStatusChanged(
		Facility facility,
		FacilityStatus oldStatus,
		FacilityStatus newStatus,
		String actor,
		String correlationId,
		Instant occurredAt) {

	public FacilityStatusChanged {
		Objects.requireNonNull(facility, "Facility must not be null.");
		Objects.requireNonNull(oldStatus, "Old facility status must not be null.");
		Objects.requireNonNull(newStatus, "New facility status must not be null.");
		Objects.requireNonNull(actor, "Actor must not be null.");
		Objects.requireNonNull(occurredAt, "Occurred-at timestamp must not be null.");
	}

	public static FacilityStatusChanged of(
			Facility facility,
			FacilityStatus oldStatus,
			FacilityStatus newStatus,
			String actor,
			String correlationId) {
		return new FacilityStatusChanged(facility, oldStatus, newStatus, actor, correlationId, Instant.now());
	}
}
