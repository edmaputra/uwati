package io.github.edmaputra.uwati.adapter.persistence.tenancy;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantRepository;
import io.github.edmaputra.uwati.domain.tenancy.domain.Tenant;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JpaTenantRegistry implements TenantRepository {

	private final TenantJpaRepository tenants;

	@Override
	public Optional<Tenant> findById(TenantId tenantId) {
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		return tenants.findById(tenantId.value()).map(this::toDomain);
	}

	@Override
	public Optional<Tenant> findByDisplayName(String displayName) {
		if (displayName == null || displayName.isBlank()) {
			return Optional.empty();
		}
		return tenants.findByDisplayNameNormalized(normalizeDisplayName(displayName)).map(this::toDomain);
	}

	@Override
	public Tenant save(Tenant tenant) {
		Objects.requireNonNull(tenant, "Tenant must not be null.");
		return toDomain(tenants.save(toEntity(tenant)));
	}

	private Tenant toDomain(TenantEntity tenantEntity) {
		return new Tenant(
				new TenantId(tenantEntity.id()),
				tenantEntity.legalName(),
				tenantEntity.displayName(),
				tenantEntity.status(),
				tenantEntity.createdAt(),
				tenantEntity.updatedAt());
	}

	private TenantEntity toEntity(Tenant tenant) {
		return new TenantEntity(
				tenant.id().value(),
				tenant.legalName(),
				tenant.displayName(),
				normalizeDisplayName(tenant.displayName()),
				tenant.status(),
				tenant.createdAt(),
				tenant.updatedAt());
	}

	private String normalizeDisplayName(String displayName) {
		return displayName.trim().toLowerCase(Locale.ROOT);
	}
}
