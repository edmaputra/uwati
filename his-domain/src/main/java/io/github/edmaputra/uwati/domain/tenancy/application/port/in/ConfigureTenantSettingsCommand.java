package io.github.edmaputra.uwati.domain.tenancy.application.port.in;

import java.util.List;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;

public record ConfigureTenantSettingsCommand(TenantId tenantId, List<SettingEntry> settings) {

	public ConfigureTenantSettingsCommand {
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		Objects.requireNonNull(settings, "Settings list must not be null.");
		if (settings.isEmpty()) {
			throw new IllegalArgumentException("Settings list must not be empty.");
		}
		settings = List.copyOf(settings);
	}

	public record SettingEntry(String key, String value) {

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
