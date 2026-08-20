package io.github.edmaputra.uwati.core.tenancy.application.service;

import java.time.Instant;
import java.util.Objects;

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

@RequiredArgsConstructor
public class CreateTenantService implements CreateTenantUseCase {

	private final TenantRepository tenantRepository;
	private final TenantEventPublisher eventPublisher;

	@Override
	public Tenant execute(CreateTenantCommand command) {
		Objects.requireNonNull(command, "Create tenant command must not be null.");

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
					eventPublisher.publish(TenantCreated.of(createdTenant));
					return createdTenant;
				});
	}
}
