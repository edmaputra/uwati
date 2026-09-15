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
import io.github.edmaputra.iam.domain.context.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.application.TenantContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing inbound use cases for healthcare facility management.
 * <p>
 * Manages creation, detail updates, status lifecycle transitions, and lookups of facilities
 * within tenant boundaries, publishing domain events upon modifications.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@RequiredArgsConstructor
public class FacilityManagementService implements
		CreateFacilityUseCase,
		UpdateFacilityUseCase,
		ChangeFacilityStatusUseCase,
		FindFacilityUseCase {

	private final TenantContext tenantContext;
	private final FacilityRepository facilityRepository;
	private final OrganizationEventPublisher eventPublisher;

	/**
	 * Creates a new facility within the current tenant context.
	 *
	 * @param command the facility creation command containing code, name, and classification
	 * @param context the operation context containing actor and correlation metadata
	 * @return the newly created and persisted facility entity
	 * @throws NullPointerException if {@code command} or {@code context} is null
	 * @throws IllegalStateException if the current tenant context is not set
	 * @throws DuplicateFacilityCodeException if a facility with the given code already exists
	 */
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

	/**
	 * Updates the details and profile of an existing facility.
	 *
	 * @param command the facility update command containing modified details
	 * @param context the operation context containing actor and correlation metadata
	 * @return the updated facility entity
	 * @throws NullPointerException if {@code command} or {@code context} is null
	 * @throws IllegalStateException if the current tenant context is not set
	 * @throws FacilityNotFoundException if the facility does not exist
	 */
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

	/**
	 * Transitions the operational status of an existing facility.
	 *
	 * @param command the facility status change command
	 * @param context the operation context containing actor and correlation metadata
	 * @return the facility entity with updated status
	 * @throws NullPointerException if {@code command} or {@code context} is null
	 * @throws IllegalStateException if the current tenant context is not set
	 * @throws FacilityNotFoundException if the facility does not exist
	 */
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

	/**
	 * Finds a facility by its unique identifier within the current tenant scope.
	 *
	 * @param id the unique facility identifier
	 * @return an {@link Optional} containing the facility if found, or empty if not found
	 * @throws NullPointerException if {@code id} is null
	 * @throws IllegalStateException if the current tenant context is not set
	 */
	@Override
	public Optional<Facility> findById(FacilityId id) {
		Objects.requireNonNull(id, "Facility ID must not be null.");
		tenantContext.requireTenantId();
		return facilityRepository.findById(id);
	}

	/**
	 * Finds all facilities matching optional type and status criteria within the current tenant scope.
	 *
	 * @param type the optional facility type filter, or {@code null} for all types
	 * @param status the optional facility status filter, or {@code null} for all statuses
	 * @return list of matching facilities
	 * @throws IllegalStateException if the current tenant context is not set
	 */
	@Override
	public List<Facility> findAll(FacilityType type, FacilityStatus status) {
		tenantContext.requireTenantId();
		return facilityRepository.findAll(type, status);
	}
}
