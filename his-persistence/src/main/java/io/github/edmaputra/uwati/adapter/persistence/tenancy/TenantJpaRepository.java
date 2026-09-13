package io.github.edmaputra.uwati.adapter.persistence.tenancy;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for tenant relational database operations.
 * <p>
 * Functions as an internal persistence mechanism in the hexagonal architecture's
 * outbound tenancy persistence adapter, providing database access for tenant records.
 *
 * @author edmaputra
 * @since 0.0.1
 */
interface TenantJpaRepository extends JpaRepository<TenantEntity, UUID> {

	/**
	 * Finds a tenant entity by its normalized display name.
	 *
	 * @param displayNameNormalized lowercased and trimmed display name
	 * @return an {@link Optional} containing the tenant entity if found, or empty otherwise
	 */
	Optional<TenantEntity> findByDisplayNameNormalized(String displayNameNormalized);
}
