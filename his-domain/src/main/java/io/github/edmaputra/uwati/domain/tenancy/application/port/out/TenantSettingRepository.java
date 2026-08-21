package io.github.edmaputra.uwati.domain.tenancy.application.port.out;

import java.util.List;
import java.util.Optional;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;

public interface TenantSettingRepository {

	List<TenantSetting> findAllByTenantId(TenantId tenantId);

	Optional<TenantSetting> findByTenantIdAndKey(TenantId tenantId, String key);

	List<TenantSetting> saveAll(List<TenantSetting> settings);
}
