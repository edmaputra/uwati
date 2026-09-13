package io.github.edmaputra.uwati.domain.organization.event;

import java.time.Instant;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.organization.ServiceUnit;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitStatus;

public record ServiceUnitStatusChanged(
		ServiceUnit serviceUnit,
		ServiceUnitStatus oldStatus,
		ServiceUnitStatus newStatus,
		String actor,
		String correlationId,
		Instant occurredAt) {

	public ServiceUnitStatusChanged {
		Objects.requireNonNull(serviceUnit, "Service unit must not be null.");
		Objects.requireNonNull(oldStatus, "Old service unit status must not be null.");
		Objects.requireNonNull(newStatus, "New service unit status must not be null.");
		Objects.requireNonNull(actor, "Actor must not be null.");
		Objects.requireNonNull(occurredAt, "Occurred-at timestamp must not be null.");
	}

	public static ServiceUnitStatusChanged of(
			ServiceUnit serviceUnit,
			ServiceUnitStatus oldStatus,
			ServiceUnitStatus newStatus,
			String actor,
			String correlationId) {
		return new ServiceUnitStatusChanged(serviceUnit, oldStatus, newStatus, actor, correlationId, Instant.now());
	}
}
