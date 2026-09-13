package io.github.edmaputra.uwati.adapter.persistence.tenancy;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for managing tenant document numbering sequences.
 * <p>
 * Functions as an internal persistence mechanism in the hexagonal architecture's
 * outbound tenancy persistence adapter for document sequence entities.
 *
 * @author edmaputra
 * @since 0.0.1
 */
interface TenantDocumentSequenceJpaRepository extends JpaRepository<TenantDocumentSequenceEntity, Long> {
}
