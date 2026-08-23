package io.github.edmaputra.uwati.domain.tenancy.application.port.in;

import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.Tenant;

public interface CreateTenantUseCase {

	Tenant execute(CreateTenantCommand command, OperationContext context);
}
