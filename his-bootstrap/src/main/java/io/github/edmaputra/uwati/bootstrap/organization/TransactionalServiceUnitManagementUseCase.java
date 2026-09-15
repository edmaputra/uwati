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
import io.github.edmaputra.iam.domain.context.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.application.TenantContext;

/**
 * Transactional decorator and Spring service bean wiring for service unit use cases.
 * <p>
 * Wraps {@link ServiceUnitManagementService} with Spring declarative transaction boundaries
 * for mutation and read-only operations.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@Service
public class TransactionalServiceUnitManagementUseCase implements
		CreateServiceUnitUseCase,
		UpdateServiceUnitUseCase,
		ChangeServiceUnitStatusUseCase,
		FindServiceUnitUseCase {

	private final ServiceUnitManagementService delegate;

	/**
	 * Constructs the transactional service unit use case with required dependencies.
	 *
	 * @param tenantContext context provider for the current tenant
	 * @param facilityRepository repository for accessing facility data
	 * @param serviceUnitRepository repository for managing service units
	 * @param eventPublisher publisher for organization events
	 */
	public TransactionalServiceUnitManagementUseCase(
			TenantContext tenantContext,
			FacilityRepository facilityRepository,
			ServiceUnitRepository serviceUnitRepository,
			OrganizationEventPublisher eventPublisher) {
		this.delegate = new ServiceUnitManagementService(tenantContext, facilityRepository, serviceUnitRepository, eventPublisher);
	}

	/**
	 * Creates a new service unit within a transaction.
	 *
	 * @param command the creation command for the service unit
	 * @param context the operation context containing actor and correlation details
	 * @return the created service unit entity
	 */
	@Override
	@Transactional
	public ServiceUnit execute(CreateServiceUnitCommand command, OperationContext context) {
		return delegate.execute(command, context);
	}

	/**
	 * Updates an existing service unit within a transaction.
	 *
	 * @param command the update command for the service unit
	 * @param context the operation context containing actor and correlation details
	 * @return the updated service unit entity
	 */
	@Override
	@Transactional
	public ServiceUnit execute(UpdateServiceUnitCommand command, OperationContext context) {
		return delegate.execute(command, context);
	}

	/**
	 * Changes the operational status of a service unit within a transaction.
	 *
	 * @param command the status transition command for the service unit
	 * @param context the operation context containing actor and correlation details
	 * @return the service unit entity with updated status
	 */
	@Override
	@Transactional
	public ServiceUnit execute(ChangeServiceUnitStatusCommand command, OperationContext context) {
		return delegate.execute(command, context);
	}

	/**
	 * Finds a service unit by its ID within a read-only transaction.
	 *
	 * @param id the unique service unit identifier
	 * @return an {@link Optional} containing the service unit if found, or empty if not found
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<ServiceUnit> findById(ServiceUnitId id) {
		return delegate.findById(id);
	}

	/**
	 * Finds service units belonging to a facility within a read-only transaction.
	 *
	 * @param facilityId the parent facility identifier
	 * @param type the optional service unit type filter, or {@code null} for all types
	 * @param status the optional status filter, or {@code null} for all statuses
	 * @return list of matching service units
	 */
	@Override
	@Transactional(readOnly = true)
	public List<ServiceUnit> findByFacilityId(FacilityId facilityId, ServiceUnitType type, ServiceUnitStatus status) {
		return delegate.findByFacilityId(facilityId, type, status);
	}
}
