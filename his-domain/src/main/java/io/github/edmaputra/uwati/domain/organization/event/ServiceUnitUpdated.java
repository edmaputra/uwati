package io.github.edmaputra.uwati.domain.organization.event;

import java.time.Instant;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.organization.ServiceUnit;

public record ServiceUnitUpdated(
		ServiceUnit previous,
		ServiceUnit current,
		String actor,
		String correlationId,
		Instant occurredAt) {

	public ServiceUnitUpdated {
		Objects.requireNonNull(previous, "Previous service unit must not be null.");
		Objects.requireNonNull(current, "Current service unit must not be null.");
		Objects.requireNonNull(actor, "Actor must not be null.");
		Objects.requireNonNull(occurredAt, "Occurred-at timestamp must not be null.");
	}

	public static ServiceUnitUpdated of(
			ServiceUnit previous,
			ServiceUnit current,
			String actor,
			String correlationId) {
		return new ServiceUnitUpdated(previous, current, actor, correlationId, Instant.now());
	}
}
