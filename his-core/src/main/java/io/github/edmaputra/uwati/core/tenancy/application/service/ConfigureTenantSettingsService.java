package io.github.edmaputra.uwati.core.tenancy.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.github.edmaputra.uwati.domain.tenancy.application.port.in.ConfigureTenantSettingsCommand;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.ConfigureTenantSettingsCommand.SettingEntry;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.ConfigureTenantSettingsUseCase;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantEventPublisher;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantRepository;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantSettingRepository;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantNotFoundException;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSettingValidator;
import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantSettingsUpdated;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConfigureTenantSettingsService implements ConfigureTenantSettingsUseCase {

	private final TenantRepository tenantRepository;
	private final TenantSettingRepository tenantSettingRepository;
	private final TenantEventPublisher tenantEventPublisher;

	@Override
	public List<TenantSetting> execute(ConfigureTenantSettingsCommand command) {
		Objects.requireNonNull(command, "Command must not be null.");

		tenantRepository.findById(command.tenantId())
				.orElseThrow(() -> new TenantNotFoundException(command.tenantId()));

		// Validate all settings first
		for (SettingEntry entry : command.settings()) {
			TenantSettingValidator.validate(entry.key(), entry.value());
		}

		List<TenantSetting> settingsToSave = new ArrayList<>();
		for (SettingEntry entry : command.settings()) {
			Optional<TenantSetting> existing =
					tenantSettingRepository.findByTenantIdAndKey(command.tenantId(), entry.key());

			TenantSetting setting = existing
					.map(current -> current.withIncrementedRevision(entry.value()))
					.orElseGet(() -> new TenantSetting(command.tenantId(), entry.key(), entry.value(), 1));

			settingsToSave.add(setting);
		}

		List<TenantSetting> saved = tenantSettingRepository.saveAll(settingsToSave);
		tenantEventPublisher.publish(TenantSettingsUpdated.of(command.tenantId(), saved));
		return saved;
	}
}
