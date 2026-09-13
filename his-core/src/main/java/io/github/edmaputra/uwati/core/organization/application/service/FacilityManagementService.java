package io.github.edmaputra.uwati.core.organization.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.github.edmaputra.uwati.domain.organization.DuplicateFacilityCodeException;
import io.github.edmaputra.uwati.domain.organization.Facility;
import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.FacilityNotFoundException;
import io.github.edmaputra.uwati.domain.organization.FacilityStatus;
import io.github.edmaputra.uwati.domain.organization.FacilityType;
import io.github.edmaputra.uwati.domain.organization.event.FacilityCreated;
import io.github.edmaputra.uwati.domain.organization.event.FacilityStatusChanged;
import io.github.edmaputra.uwati.domain.organization.event.FacilityUpdated;
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
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FacilityManagementService implements
		CreateFacilityUseCase,
		UpdateFacilityUseCase,
		ChangeFacilityStatusUseCase,
		FindFacilityUseCase {

	private final TenantContext tenantContext;
	private final FacilityRepository facilityRepository;
	private final OrganizationEventPublisher eventPublisher;

	@Override
	public Facility execute(CreateFacilityCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Create facility command must not be null.");
		Objects.requireNonNull(context, "Operation context must not be null.");

		TenantId tenantId = tenantContext.requireTenantId();
		String code = command.code().trim();
		String name = command.name().trim();

		if (facilityRepository.existsByCode(code)) {
			throw new DuplicateFacilityCodeException(code);
		}

		Instant now = Instant.now();
		Facility facility = new Facility(
				FacilityId.generate(),
				tenantId,
				code,
				name,
				command.type(),
				command.classification(),
				command.nationalRegistryCode(),
				command.scopeNodeId(),
				command.address(),
				command.phone(),
				FacilityStatus.ACTIVE,
				now,
				now);

		Facility saved = facilityRepository.save(facility);
		eventPublisher.publish(FacilityCreated.of(saved, context.actor(), context.correlationId()));
		return saved;
	}

	@Override
	public Facility execute(UpdateFacilityCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Update facility command must not be null.");
		Objects.requireNonNull(context, "Operation context must not be null.");

		tenantContext.requireTenantId();
		Facility existing = facilityRepository.findById(command.id())
				.orElseThrow(() -> new FacilityNotFoundException(command.id()));

		Facility updated = existing.update(
				command.name().trim(),
				command.type(),
				command.classification(),
				command.nationalRegistryCode(),
				command.scopeNodeId(),
				command.address(),
				command.phone());

		Facility saved = facilityRepository.save(updated);
		eventPublisher.publish(FacilityUpdated.of(existing, saved, context.actor(), context.correlationId()));
		return saved;
	}

	@Override
	public Facility execute(ChangeFacilityStatusCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Change facility status command must not be null.");
		Objects.requireNonNull(context, "Operation context must not be null.");

		tenantContext.requireTenantId();
		Facility existing = facilityRepository.findById(command.id())
				.orElseThrow(() -> new FacilityNotFoundException(command.id()));

		FacilityStatus oldStatus = existing.status();
		if (oldStatus == command.status()) {
			return existing;
		}

		Facility changed = existing.changeStatus(command.status());
		Facility saved = facilityRepository.save(changed);
		eventPublisher.publish(FacilityStatusChanged.of(saved, oldStatus, command.status(), context.actor(), context.correlationId()));
		return saved;
	}

	@Override
	public Optional<Facility> findById(FacilityId id) {
		Objects.requireNonNull(id, "Facility ID must not be null.");
		tenantContext.requireTenantId();
		return facilityRepository.findById(id);
	}

	@Override
	public List<Facility> findAll(FacilityType type, FacilityStatus status) {
		tenantContext.requireTenantId();
		return facilityRepository.findAll(type, status);
	}
}
