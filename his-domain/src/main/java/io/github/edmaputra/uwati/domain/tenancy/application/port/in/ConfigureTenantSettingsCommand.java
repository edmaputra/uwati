package io.github.edmaputra.uwati.domain.tenancy.application.port.in;

import java.util.List;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;

/**
 * Inbound command encapsulating the request to configure or update tenant settings.
 * <p>
 * Passed across the inbound port boundary to {@link ConfigureTenantSettingsUseCase}
 * in the application layer of the Hexagonal Architecture.
 *
 * @param tenantId the identifier of the tenant whose settings are being configured
 * @param settings the non-empty list of key-value configuration entries
 * @author edmaputra
 * @since 0.0.1
 */
public record ConfigureTenantSettingsCommand(TenantId tenantId, List<SettingEntry> settings) {

	/**
	 * Compact constructor validating invariant constraints and defensively copying settings.
	 *
	 * @param tenantId the tenant ID
	 * @param settings the list of setting entries
	 * @throws NullPointerException if {@code tenantId} or {@code settings} is null
	 * @throws IllegalArgumentException if {@code settings} is empty
	 */
	public ConfigureTenantSettingsCommand {
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		Objects.requireNonNull(settings, "Settings list must not be null.");
		if (settings.isEmpty()) {
			throw new IllegalArgumentException("Settings list must not be empty.");
		}
		settings = List.copyOf(settings);
	}

	/**
	 * Represents an individual key-value configuration pair within the configuration command.
	 *
	 * @param key the setting key identifier
	 * @param value the setting value string
	 * @author edmaputra
	 * @since 0.0.1
	 */
	public record SettingEntry(String key, String value) {

		/**
		 * Compact constructor validating that the key and value are present and non-blank.
		 *
		 * @param key the setting key
		 * @param value the setting value
		 * @throws IllegalArgumentException if {@code key} or {@code value} is null or blank
		 */
		public SettingEntry {
			if (key == null || key.isBlank()) {
				throw new IllegalArgumentException("Setting key must not be blank.");
			}
			if (value == null || value.isBlank()) {
				throw new IllegalArgumentException("Setting value must not be blank.");
			}
		}
	}
}
