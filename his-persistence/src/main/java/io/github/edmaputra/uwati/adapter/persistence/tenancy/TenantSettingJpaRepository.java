package io.github.edmaputra.uwati.adapter.persistence.tenancy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface TenantSettingJpaRepository extends JpaRepository<TenantSettingEntity, Long> {

	List<TenantSettingEntity> findAllByTenantId(UUID tenantId);

	Optional<TenantSettingEntity> findByTenantIdAndSettingKey(UUID tenantId, String settingKey);
}
