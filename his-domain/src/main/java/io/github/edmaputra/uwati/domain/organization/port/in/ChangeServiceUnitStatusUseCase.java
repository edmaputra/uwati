package io.github.edmaputra.uwati.domain.organization.port.in;

import io.github.edmaputra.uwati.domain.organization.ServiceUnit;
import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;

public interface ChangeServiceUnitStatusUseCase {

	ServiceUnit execute(ChangeServiceUnitStatusCommand command, OperationContext context);
}
