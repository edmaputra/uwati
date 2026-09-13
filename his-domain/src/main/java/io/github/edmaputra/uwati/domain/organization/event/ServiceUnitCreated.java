package io.github.edmaputra.uwati.domain.organization.event;

import java.time.Instant;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.organization.ServiceUnit;

public record ServiceUnitCreated(
		ServiceUnit serviceUnit,
		String actor,
		String correlationId,
		Instant occurredAt) {

	public ServiceUnitCreated {
		Objects.requireNonNull(serviceUnit, "Service unit must not be null.");
		Objects.requireNonNull(actor, "Actor must not be null.");
		Objects.requireNonNull(occurredAt, "Occurred-at timestamp must not be null.");
	}

	public static ServiceUnitCreated of(ServiceUnit serviceUnit, String actor, String correlationId) {
		return new ServiceUnitCreated(serviceUnit, actor, correlationId, Instant.now());
	}
}
