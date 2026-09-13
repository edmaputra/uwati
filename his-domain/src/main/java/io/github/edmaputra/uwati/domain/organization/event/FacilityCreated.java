package io.github.edmaputra.uwati.domain.organization.event;

import java.time.Instant;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.organization.Facility;

public record FacilityCreated(Facility facility, String actor, String correlationId, Instant occurredAt) {

	public FacilityCreated {
		Objects.requireNonNull(facility, "Facility must not be null.");
		Objects.requireNonNull(actor, "Actor must not be null.");
		Objects.requireNonNull(occurredAt, "Occurred-at timestamp must not be null.");
	}

	public static FacilityCreated of(Facility facility, String actor, String correlationId) {
		return new FacilityCreated(facility, actor, correlationId, Instant.now());
	}
}
