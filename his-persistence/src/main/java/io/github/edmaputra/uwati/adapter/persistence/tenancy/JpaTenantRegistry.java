package io.github.edmaputra.uwati.adapter.persistence.tenancy;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantRepository;
import io.github.edmaputra.uwati.domain.tenancy.domain.Tenant;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import lombok.RequiredArgsConstructor;

/**
 * Secondary (outbound) persistence adapter implementing {@link TenantRepository} using Spring Data JPA.
 * <p>
 * Bridges domain-driven tenancy port interfaces to relational database persistence in the
 * hexagonal architecture. Manages tenant record lifecycle, display name normalization,
 * and mapping between JPA entities and domain models.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class JpaTenantRegistry implements TenantRepository {

	private final TenantJpaRepository tenants;

	/**
	 * Finds a tenant by its unique identifier.
	 *
	 * @param tenantId unique identifier of the tenant
	 * @return an {@link Optional} containing the tenant if found, or empty if not found
	 * @throws NullPointerException if {@code tenantId} is null
	 */
	@Override
	public Optional<Tenant> findById(TenantId tenantId) {
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		return tenants.findById(tenantId.value()).map(this::toDomain);
	}

	/**
	 * Finds a tenant by its display name ignoring case and leading/trailing whitespace.
	 *
	 * @param displayName tenant display name to search for
	 * @return an {@link Optional} containing the tenant if found, or empty if not found or blank
	 */
	@Override
	public Optional<Tenant> findByDisplayName(String displayName) {
		if (displayName == null || displayName.isBlank()) {
			return Optional.empty();
		}
		return tenants.findByDisplayNameNormalized(normalizeDisplayName(displayName)).map(this::toDomain);
	}

	/**
	 * Persists a tenant domain entity into relational storage.
	 *
	 * @param tenant domain model representing the tenant to persist
	 * @return the persisted tenant domain model
	 * @throws NullPointerException if {@code tenant} is null
	 */
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
