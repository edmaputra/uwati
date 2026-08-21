package io.github.edmaputra.uwati.domain.tenancy.application.port.in;

import java.util.List;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;

public interface ConfigureTenantSettingsUseCase {

	List<TenantSetting> execute(ConfigureTenantSettingsCommand command);
}
