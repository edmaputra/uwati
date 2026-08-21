package io.github.edmaputra.uwati.bootstrap.tenancy;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.edmaputra.uwati.core.tenancy.application.service.ConfigureTenantSettingsService;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.ConfigureTenantSettingsCommand;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.ConfigureTenantSettingsUseCase;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantEventPublisher;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantRepository;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantSettingRepository;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;

@Service
public class TransactionalConfigureTenantSettingsUseCase implements ConfigureTenantSettingsUseCase {

	private final ConfigureTenantSettingsService delegate;

	public TransactionalConfigureTenantSettingsUseCase(
			TenantRepository tenantRepository,
			TenantSettingRepository tenantSettingRepository,
			TenantEventPublisher eventPublisher) {
		this.delegate = new ConfigureTenantSettingsService(tenantRepository, tenantSettingRepository, eventPublisher);
	}

	@Override
	@Transactional
	public List<TenantSetting> execute(ConfigureTenantSettingsCommand command) {
		return delegate.execute(command);
	}
}
