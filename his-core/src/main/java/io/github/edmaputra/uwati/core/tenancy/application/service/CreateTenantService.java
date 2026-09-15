package io.github.edmaputra.uwati.core.tenancy.application.service;

import java.time.Instant;
import java.util.Objects;

import io.github.edmaputra.iam.domain.context.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.CreateTenantCommand;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.CreateTenantUseCase;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantEventPublisher;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantRepository;
import io.github.edmaputra.uwati.domain.tenancy.domain.DuplicateTenantDisplayNameException;
import io.github.edmaputra.uwati.domain.tenancy.domain.Tenant;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantStatus;
import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantCreated;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing {@link CreateTenantUseCase}.
 * <p>
 * Handles creation of new tenants or returns existing matching tenants, ensuring uniqueness
 * of display names and publishing {@link TenantCreated} domain events upon creation.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@RequiredArgsConstructor
public class CreateTenantService implements CreateTenantUseCase {

	private final TenantRepository tenantRepository;
	private final TenantEventPublisher eventPublisher;

	/**
	 * Creates a new tenant or returns an existing tenant matching legal and display names.
	 *
	 * @param command the tenant creation command containing legal and display names
	 * @param context the operation context containing actor and correlation identifiers
	 * @return the newly created or existing matching tenant entity
	 * @throws NullPointerException if {@code command} or {@code context} is null
	 * @throws IllegalArgumentException if legal name or display name is blank
	 * @throws DuplicateTenantDisplayNameException if display name already exists for a different legal entity
	 */
	@Override
	public Tenant execute(CreateTenantCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Create tenant command must not be null.");
		Objects.requireNonNull(context, "Operation context must not be null.");

		String legalName = command.legalName().trim();
		String displayName = command.displayName().trim();
		if (legalName.isBlank()) {
			throw new IllegalArgumentException("Tenant legal name must not be blank.");
		}
		if (displayName.isBlank()) {
			throw new IllegalArgumentException("Tenant display name must not be blank.");
		}

		return tenantRepository.findByDisplayName(displayName)
				.map(existing -> {
					if (!existing.legalName().equalsIgnoreCase(legalName)) {
						throw new DuplicateTenantDisplayNameException(displayName);
					}
					return existing;
				})
				.orElseGet(() -> {
					Instant now = Instant.now();
					Tenant tenant = new Tenant(
							TenantId.generate(),
							legalName,
							displayName,
							TenantStatus.ACTIVE,
							now,
							now);
					Tenant createdTenant = tenantRepository.save(tenant);
					eventPublisher.publish(
							TenantCreated.of(createdTenant, context.actor(), context.correlationId()));
					return createdTenant;
				});
	}
}
