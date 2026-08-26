package io.github.edmaputra.uwati.domain.tenancy.domain.event;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;

public record TenantSettingsUpdated(
		TenantId tenantId,
		List<TenantSetting> previousSettings,
		List<TenantSetting> updatedSettings,
		String actor,
		String correlationId,
		Instant occurredAt) {

	public TenantSettingsUpdated {
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		Objects.requireNonNull(previousSettings, "Previous settings must not be null.");
		Objects.requireNonNull(updatedSettings, "Updated settings must not be null.");
		Objects.requireNonNull(actor, "Actor must not be null.");
		Objects.requireNonNull(occurredAt, "Occurred-at timestamp must not be null.");
	}

	public static TenantSettingsUpdated of(TenantId tenantId, List<TenantSetting> previousSettings,
			List<TenantSetting> updatedSettings, String actor, String correlationId) {
		return new TenantSettingsUpdated(tenantId, List.copyOf(previousSettings),
				List.copyOf(updatedSettings), actor, correlationId, Instant.now());
	}
}
