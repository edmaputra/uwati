package io.github.edmaputra.uwati.domain.organization.port.in;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.organization.ServiceUnitId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitType;

public record UpdateServiceUnitCommand(
		ServiceUnitId id,
		String name,
		ServiceUnitType type,
		UUID scopeNodeId) {

	public UpdateServiceUnitCommand {
		Objects.requireNonNull(id, "Service Unit ID must not be null.");
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Service unit name must not be blank.");
		}
		Objects.requireNonNull(type, "Service unit type must not be null.");
	}
}
