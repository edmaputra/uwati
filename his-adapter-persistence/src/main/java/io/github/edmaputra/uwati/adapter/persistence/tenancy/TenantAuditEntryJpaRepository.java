package io.github.edmaputra.uwati.adapter.persistence.tenancy;

import org.springframework.data.jpa.repository.JpaRepository;

interface TenantAuditEntryJpaRepository extends JpaRepository<TenantAuditEntryEntity, Long> {
}
