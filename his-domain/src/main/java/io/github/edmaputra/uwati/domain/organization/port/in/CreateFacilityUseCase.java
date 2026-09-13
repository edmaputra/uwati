package io.github.edmaputra.uwati.domain.organization.port.in;

import io.github.edmaputra.uwati.domain.organization.Facility;
import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;

public interface CreateFacilityUseCase {

	Facility execute(CreateFacilityCommand command, OperationContext context);
}
