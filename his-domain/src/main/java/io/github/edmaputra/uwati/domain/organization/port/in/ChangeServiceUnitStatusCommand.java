package io.github.edmaputra.uwati.domain.organization.port.in;

import java.util.Objects;

import io.github.edmaputra.uwati.domain.organization.ServiceUnitId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitStatus;

public record ChangeServiceUnitStatusCommand(ServiceUnitId id, ServiceUnitStatus status) {

	public ChangeServiceUnitStatusCommand {
		Objects.requireNonNull(id, "Service Unit ID must not be null.");
		Objects.requireNonNull(status, "Service Unit status must not be null.");
	}
}
