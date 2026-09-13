package io.github.edmaputra.uwati.core.organization.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.github.edmaputra.uwati.domain.organization.DuplicateServiceUnitCodeException;
import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.FacilityNotFoundException;
import io.github.edmaputra.uwati.domain.organization.ServiceUnit;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitNotFoundException;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitStatus;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitType;
import io.github.edmaputra.uwati.domain.organization.event.ServiceUnitCreated;
import io.github.edmaputra.uwati.domain.organization.event.ServiceUnitStatusChanged;
import io.github.edmaputra.uwati.domain.organization.event.ServiceUnitUpdated;
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
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServiceUnitManagementService implements
		CreateServiceUnitUseCase,
		UpdateServiceUnitUseCase,
		ChangeServiceUnitStatusUseCase,
		FindServiceUnitUseCase {

	private final TenantContext tenantContext;
	private final FacilityRepository facilityRepository;
	private final ServiceUnitRepository serviceUnitRepository;
	private final OrganizationEventPublisher eventPublisher;

	@Override
	public ServiceUnit execute(CreateServiceUnitCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Create service unit command must not be null.");
		Objects.requireNonNull(context, "Operation context must not be null.");

		TenantId tenantId = tenantContext.requireTenantId();
		String code = command.code().trim();
		String name = command.name().trim();

		// Verify parent facility exists
		facilityRepository.findById(command.facilityId())
				.orElseThrow(() -> new FacilityNotFoundException(command.facilityId()));

		if (serviceUnitRepository.existsByFacilityIdAndCode(command.facilityId(), code)) {
			throw new DuplicateServiceUnitCodeException(code);
		}

		Instant now = Instant.now();
		ServiceUnit serviceUnit = new ServiceUnit(
				ServiceUnitId.generate(),
				tenantId,
				command.facilityId(),
				code,
				name,
				command.type(),
				command.scopeNodeId(),
				ServiceUnitStatus.ACTIVE,
				now,
				now);

		ServiceUnit saved = serviceUnitRepository.save(serviceUnit);
		eventPublisher.publish(ServiceUnitCreated.of(saved, context.actor(), context.correlationId()));
		return saved;
	}

	@Override
	public ServiceUnit execute(UpdateServiceUnitCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Update service unit command must not be null.");
		Objects.requireNonNull(context, "Operation context must not be null.");

		tenantContext.requireTenantId();
		ServiceUnit existing = serviceUnitRepository.findById(command.id())
				.orElseThrow(() -> new ServiceUnitNotFoundException(command.id()));

		ServiceUnit updated = existing.update(command.name().trim(), command.type(), command.scopeNodeId());
		ServiceUnit saved = serviceUnitRepository.save(updated);
		eventPublisher.publish(ServiceUnitUpdated.of(existing, saved, context.actor(), context.correlationId()));
		return saved;
	}

	@Override
	public ServiceUnit execute(ChangeServiceUnitStatusCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Change service unit status command must not be null.");
		Objects.requireNonNull(context, "Operation context must not be null.");

		tenantContext.requireTenantId();
		ServiceUnit existing = serviceUnitRepository.findById(command.id())
				.orElseThrow(() -> new ServiceUnitNotFoundException(command.id()));

		ServiceUnitStatus oldStatus = existing.status();
		if (oldStatus == command.status()) {
			return existing;
		}

		ServiceUnit changed = existing.changeStatus(command.status());
		ServiceUnit saved = serviceUnitRepository.save(changed);
		eventPublisher.publish(ServiceUnitStatusChanged.of(saved, oldStatus, command.status(), context.actor(), context.correlationId()));
		return saved;
	}

	@Override
	public Optional<ServiceUnit> findById(ServiceUnitId id) {
		Objects.requireNonNull(id, "Service Unit ID must not be null.");
		tenantContext.requireTenantId();
		return serviceUnitRepository.findById(id);
	}

	@Override
	public List<ServiceUnit> findByFacilityId(FacilityId facilityId, ServiceUnitType type, ServiceUnitStatus status) {
		Objects.requireNonNull(facilityId, "Facility ID must not be null.");
		tenantContext.requireTenantId();
		return serviceUnitRepository.findByFacilityId(facilityId, type, status);
	}
}
