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

/**
 * Transactional decorator and Spring service bean wiring for facility use cases.
 * <p>
 * Wraps {@link FacilityManagementService} with Spring declarative transaction boundaries
 * for mutation and read-only operations.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@Service
public class TransactionalFacilityManagementUseCase implements
		CreateFacilityUseCase,
		UpdateFacilityUseCase,
		ChangeFacilityStatusUseCase,
		FindFacilityUseCase {

	private final FacilityManagementService delegate;

	/**
	 * Constructs the transactional facility use case with required dependencies.
	 *
	 * @param tenantContext context provider for current tenant
	 * @param facilityRepository repository for facility entities
	 * @param eventPublisher publisher for organization events
	 */
	public TransactionalFacilityManagementUseCase(
			TenantContext tenantContext,
			FacilityRepository facilityRepository,
			OrganizationEventPublisher eventPublisher) {
		this.delegate = new FacilityManagementService(tenantContext, facilityRepository, eventPublisher);
	}

	/**
	 * Creates a new facility within a transaction.
	 *
	 * @param command the facility creation command
	 * @param context the operation context containing actor and correlation metadata
	 * @return the created facility entity
	 */
	@Override
	@Transactional
	public Facility execute(CreateFacilityCommand command, OperationContext context) {
		return delegate.execute(command, context);
	}

	/**
	 * Updates an existing facility within a transaction.
	 *
	 * @param command the facility update command
	 * @param context the operation context containing actor and correlation metadata
	 * @return the updated facility entity
	 */
	@Override
	@Transactional
	public Facility execute(UpdateFacilityCommand command, OperationContext context) {
		return delegate.execute(command, context);
	}

	/**
	 * Changes facility operational status within a transaction.
	 *
	 * @param command the facility status change command
	 * @param context the operation context containing actor and correlation metadata
	 * @return the facility entity with updated status
	 */
	@Override
	@Transactional
	public Facility execute(ChangeFacilityStatusCommand command, OperationContext context) {
		return delegate.execute(command, context);
	}

	/**
	 * Finds a facility by its identifier within a read-only transaction.
	 *
	 * @param id the unique facility identifier
	 * @return an {@link Optional} containing the facility if found, or empty if not found
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<Facility> findById(FacilityId id) {
		return delegate.findById(id);
	}

	/**
	 * Finds all facilities matching optional filters within a read-only transaction.
	 *
	 * @param type the optional facility type filter, or {@code null}
	 * @param status the optional facility status filter, or {@code null}
	 * @return list of matching facilities
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Facility> findAll(FacilityType type, FacilityStatus status) {
		return delegate.findAll(type, status);
	}
}
