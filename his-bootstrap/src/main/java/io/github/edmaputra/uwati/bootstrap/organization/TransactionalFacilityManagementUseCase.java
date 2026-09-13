package io.github.edmaputra.uwati.bootstrap.organization;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.edmaputra.uwati.core.organization.application.service.FacilityManagementService;
import io.github.edmaputra.uwati.domain.organization.Facility;
import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.FacilityStatus;
import io.github.edmaputra.uwati.domain.organization.FacilityType;
import io.github.edmaputra.uwati.domain.organization.port.in.ChangeFacilityStatusCommand;
import io.github.edmaputra.uwati.domain.organization.port.in.ChangeFacilityStatusUseCase;
import io.github.edmaputra.uwati.domain.organization.port.in.CreateFacilityCommand;
import io.github.edmaputra.uwati.domain.organization.port.in.CreateFacilityUseCase;
import io.github.edmaputra.uwati.domain.organization.port.in.FindFacilityUseCase;
import io.github.edmaputra.uwati.domain.organization.port.in.UpdateFacilityCommand;
import io.github.edmaputra.uwati.domain.organization.port.in.UpdateFacilityUseCase;
import io.github.edmaputra.uwati.domain.organization.port.out.FacilityRepository;
import io.github.edmaputra.uwati.domain.organization.port.out.OrganizationEventPublisher;
import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.application.TenantContext;

@Service
public class TransactionalFacilityManagementUseCase implements
		CreateFacilityUseCase,
		UpdateFacilityUseCase,
		ChangeFacilityStatusUseCase,
		FindFacilityUseCase {

	private final FacilityManagementService delegate;

	public TransactionalFacilityManagementUseCase(
			TenantContext tenantContext,
			FacilityRepository facilityRepository,
			OrganizationEventPublisher eventPublisher) {
		this.delegate = new FacilityManagementService(tenantContext, facilityRepository, eventPublisher);
	}

	@Override
	@Transactional
	public Facility execute(CreateFacilityCommand command, OperationContext context) {
		return delegate.execute(command, context);
	}

	@Override
	@Transactional
	public Facility execute(UpdateFacilityCommand command, OperationContext context) {
		return delegate.execute(command, context);
	}

	@Override
	@Transactional
	public Facility execute(ChangeFacilityStatusCommand command, OperationContext context) {
		return delegate.execute(command, context);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Facility> findById(FacilityId id) {
		return delegate.findById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Facility> findAll(FacilityType type, FacilityStatus status) {
		return delegate.findAll(type, status);
	}
}
