package io.github.edmaputra.uwati.domain.tenancy.domain.event;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;

public record TenantSettingsUpdated(TenantId tenantId, List<TenantSetting> updatedSettings, Instant occurredAt) {

	public TenantSettingsUpdated {
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		Objects.requireNonNull(updatedSettings, "Updated settings must not be null.");
		Objects.requireNonNull(occurredAt, "Occurred-at timestamp must not be null.");
	}

	public static TenantSettingsUpdated of(TenantId tenantId, List<TenantSetting> updatedSettings) {
		return new TenantSettingsUpdated(tenantId, List.copyOf(updatedSettings), Instant.now());
	}
}
