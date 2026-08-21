package io.github.edmaputra.uwati.bootstrap.tenancy;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.edmaputra.uwati.core.tenancy.application.service.GetTenantSettingsService;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.GetTenantSettingsUseCase;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantRepository;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantSettingRepository;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;

@Service
public class TransactionalGetTenantSettingsUseCase implements GetTenantSettingsUseCase {

	private final GetTenantSettingsService delegate;

	public TransactionalGetTenantSettingsUseCase(
			TenantRepository tenantRepository,
			TenantSettingRepository tenantSettingRepository) {
		this.delegate = new GetTenantSettingsService(tenantRepository, tenantSettingRepository);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TenantSetting> execute(TenantId tenantId) {
		return delegate.execute(tenantId);
	}
}
