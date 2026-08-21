package io.github.edmaputra.uwati.core.tenancy.application.service;

import java.util.List;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.tenancy.application.port.in.GetTenantSettingsUseCase;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantRepository;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantSettingRepository;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantNotFoundException;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetTenantSettingsService implements GetTenantSettingsUseCase {

	private final TenantRepository tenantRepository;
	private final TenantSettingRepository tenantSettingRepository;

	@Override
	public List<TenantSetting> execute(TenantId tenantId) {
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");

		tenantRepository.findById(tenantId)
				.orElseThrow(() -> new TenantNotFoundException(tenantId));

		return tenantSettingRepository.findAllByTenantId(tenantId);
	}
}
