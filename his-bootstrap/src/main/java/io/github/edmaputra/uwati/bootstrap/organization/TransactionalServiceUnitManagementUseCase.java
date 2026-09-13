package io.github.edmaputra.uwati.bootstrap.organization;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.edmaputra.uwati.core.organization.application.service.ServiceUnitManagementService;
import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnit;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitStatus;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitType;
import io.github.edmaputra.uwati.domain.organization.port.in.ChangeServiceUnitStatusCommand;
import io.github.edmaputra.uwati.domain.organization.port.in.ChangeServiceUnitStatusUseCase;
import io.github.edmaputra.uwati.domain.organization.port.in.CreateServiceUnitCommand;
import io.github.edmaputra.uwati.domain.organization.port.in.CreateServiceUnitUseCase;
import io.github.edmaputra.uwati.domain.organization.port.in.FindServiceUnitUseCase;
import io.github.edmaputra.uwati.domain.organization.port.in.UpdateServiceUnitCommand;
import io.github.edmaputra.uwati.domain.organization.port.in.UpdateServiceUnitUseCase;
import io.github.edmaputra.uwati.domain.organization.port.out.FacilityRepository;
import io.github.edmaputra.uwati.domain.organization.port.out.OrganizationEventPublisher;
import io.github.edmaputra.uwati.domain.organization.port.out.ServiceUnitRepository;
import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.application.TenantContext;

@Service
public class TransactionalServiceUnitManagementUseCase implements
		CreateServiceUnitUseCase,
		UpdateServiceUnitUseCase,
		ChangeServiceUnitStatusUseCase,
		FindServiceUnitUseCase {

	private final ServiceUnitManagementService delegate;

	public TransactionalServiceUnitManagementUseCase(
			TenantContext tenantContext,
			FacilityRepository facilityRepository,
			ServiceUnitRepository serviceUnitRepository,
			OrganizationEventPublisher eventPublisher) {
		this.delegate = new ServiceUnitManagementService(tenantContext, facilityRepository, serviceUnitRepository, eventPublisher);
	}

	@Override
	@Transactional
	public ServiceUnit execute(CreateServiceUnitCommand command, OperationContext context) {
		return delegate.execute(command, context);
	}

	@Override
	@Transactional
	public ServiceUnit execute(UpdateServiceUnitCommand command, OperationContext context) {
		return delegate.execute(command, context);
	}

	@Override
	@Transactional
	public ServiceUnit execute(ChangeServiceUnitStatusCommand command, OperationContext context) {
		return delegate.execute(command, context);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<ServiceUnit> findById(ServiceUnitId id) {
		return delegate.findById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ServiceUnit> findByFacilityId(FacilityId facilityId, ServiceUnitType type, ServiceUnitStatus status) {
		return delegate.findByFacilityId(facilityId, type, status);
	}
}
