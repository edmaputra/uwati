package io.github.edmaputra.uwati.adapter.persistence.tenancy;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface TenantJpaRepository extends JpaRepository<TenantEntity, UUID> {

	Optional<TenantEntity> findByDisplayNameNormalized(String displayNameNormalized);
}
