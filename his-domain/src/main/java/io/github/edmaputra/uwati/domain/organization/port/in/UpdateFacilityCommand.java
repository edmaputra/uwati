package io.github.edmaputra.uwati.domain.organization.port.in;

import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.organization.FacilityClassification;
import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.FacilityType;

public record UpdateFacilityCommand(
		FacilityId id,
		String name,
		FacilityType type,
		FacilityClassification classification,
		String nationalRegistryCode,
		UUID scopeNodeId,
		String address,
		String phone) {

	public UpdateFacilityCommand {
		Objects.requireNonNull(id, "Facility ID must not be null.");
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Facility name must not be blank.");
		}
		Objects.requireNonNull(type, "Facility type must not be null.");
		Objects.requireNonNull(classification, "Facility classification must not be null.");
	}
}
