package io.github.edmaputra.uwati.adapter.persistence.tenancy;

import org.springframework.data.jpa.repository.JpaRepository;

interface TenantSettingJpaRepository extends JpaRepository<TenantSettingEntity, Long> {
}
