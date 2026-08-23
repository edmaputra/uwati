package io.github.edmaputra.uwati.core.tenancy.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.ConfigureTenantSettingsCommand;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.ConfigureTenantSettingsCommand.SettingEntry;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.ConfigureTenantSettingsUseCase;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantEventPublisher;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantRepository;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantSettingRepository;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantNotFoundException;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSettingValidator;
import io.github.edmaputra.uwati.domain.tenancy.domain.event.SettingChange;
import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantSettingsUpdated;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConfigureTenantSettingsService implements ConfigureTenantSettingsUseCase {

	private final TenantRepository tenantRepository;
	private final TenantSettingRepository tenantSettingRepository;
	private final TenantEventPublisher tenantEventPublisher;

	@Override
	public List<TenantSetting> execute(ConfigureTenantSettingsCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Command must not be null.");
		Objects.requireNonNull(context, "Operation context must not be null.");

		tenantRepository.findById(command.tenantId())
				.orElseThrow(() -> new TenantNotFoundException(command.tenantId()));

		// Validate all settings first
		for (SettingEntry entry : command.settings()) {
			TenantSettingValidator.validate(entry.key(), entry.value());
		}

		List<TenantSetting> settingsToSave = new ArrayList<>();
		List<SettingChange> changes = new ArrayList<>();

		for (SettingEntry entry : command.settings()) {
			Optional<TenantSetting> existing =
					tenantSettingRepository.findByTenantIdAndKey(command.tenantId(), entry.key());

			TenantSetting setting;
			if (existing.isPresent()) {
				TenantSetting current = existing.get();
				setting = current.withIncrementedRevision(entry.value());
				if (!current.value().equals(entry.value())) {
					changes.add(SettingChange.changed(
							entry.key(), current.value(), entry.value(),
							current.revision(), setting.revision()));
				}
			} else {
				setting = new TenantSetting(command.tenantId(), entry.key(), entry.value(), 1);
				changes.add(SettingChange.added(entry.key(), entry.value(), 1));
			}

			settingsToSave.add(setting);
		}

		List<TenantSetting> saved = tenantSettingRepository.saveAll(settingsToSave);
		tenantEventPublisher.publish(
				TenantSettingsUpdated.of(command.tenantId(), saved, changes,
						context.actor(), context.correlationId()));
		return saved;
	}
}
