package io.github.edmaputra.uwati.domain.organization.port.in;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitType;

public record CreateServiceUnitCommand(
		FacilityId facilityId,
		String code,
		String name,
		ServiceUnitType type,
		UUID scopeNodeId) {

	public CreateServiceUnitCommand {
		Objects.requireNonNull(facilityId, "Facility ID must not be null.");
		if (code == null || code.isBlank()) {
			throw new IllegalArgumentException("Service unit code must not be blank.");
		}
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Service unit name must not be blank.");
		}
		Objects.requireNonNull(type, "Service unit type must not be null.");
	}
}
