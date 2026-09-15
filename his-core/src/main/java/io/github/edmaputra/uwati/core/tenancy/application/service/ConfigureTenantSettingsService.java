package io.github.edmaputra.uwati.core.tenancy.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.github.edmaputra.iam.domain.context.OperationContext;
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

/**
 * Application service implementing {@link ConfigureTenantSettingsUseCase}.
 * <p>
 * Validates setting values against domain rules, tracks previous values for audit diff calculation,
 * increments revisions, persists changes, and publishes {@link TenantSettingsUpdated} domain events.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@RequiredArgsConstructor
public class ConfigureTenantSettingsService implements ConfigureTenantSettingsUseCase {

	private final TenantRepository tenantRepository;
	private final TenantSettingRepository tenantSettingRepository;
	private final TenantEventPublisher tenantEventPublisher;

	/**
	 * Configures or updates tenant settings according to the provided command.
	 *
	 * @param command the configuration command containing tenant ID and setting entries
	 * @param context the operation context with actor and correlation details
	 * @return the list of saved or updated tenant settings
	 * @throws NullPointerException if {@code command} or {@code context} is null
	 * @throws TenantNotFoundException if the target tenant does not exist
	 * @throws io.github.edmaputra.uwati.domain.tenancy.domain.InvalidTenantSettingException if any setting key is unsupported or value fails validation
	 */
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

		List<TenantSetting> previousSettings = new ArrayList<>();
		List<TenantSetting> settingsToSave = new ArrayList<>();

		for (SettingEntry entry : command.settings()) {
			Optional<TenantSetting> existing =
					tenantSettingRepository.findByTenantIdAndKey(command.tenantId(), entry.key());

			existing.ifPresent(previousSettings::add);

			TenantSetting setting = existing
					.map(current -> current.withIncrementedRevision(entry.value()))
					.orElseGet(() -> new TenantSetting(command.tenantId(), entry.key(), entry.value(), 1));

			settingsToSave.add(setting);
		}

		List<TenantSetting> saved = tenantSettingRepository.saveAll(settingsToSave);
		tenantEventPublisher.publish(
				TenantSettingsUpdated.of(command.tenantId(), previousSettings, saved,
						context.actor(), context.correlationId()));
		return saved;
	}
}
