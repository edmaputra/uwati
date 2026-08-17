package io.github.edmaputra.uwati.core.tenancy.application.port.in;

import io.github.edmaputra.uwati.core.tenancy.domain.Tenant;

public interface CreateTenantUseCase {

	Tenant execute(CreateTenantCommand command);
}
