package io.github.edmaputra.uwati.bootstrap.tenancy;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.edmaputra.uwati.core.tenancy.application.service.CreateTenantService;
import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.CreateTenantCommand;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.CreateTenantUseCase;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantEventPublisher;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantRepository;
import io.github.edmaputra.uwati.domain.tenancy.domain.Tenant;

@Service
public class TransactionalCreateTenantUseCase implements CreateTenantUseCase {

	private final CreateTenantService delegate;

	public TransactionalCreateTenantUseCase(TenantRepository tenantRepository, TenantEventPublisher eventPublisher) {
		this.delegate = new CreateTenantService(tenantRepository, eventPublisher);
	}

	@Override
	@Transactional
	public Tenant execute(CreateTenantCommand command, OperationContext context) {
		return delegate.execute(command, context);
	}
}

