package io.github.edmaputra.uwati.domain.tenancy.domain.event;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;

/**
 * Domain event published when configuration settings for a {@link io.github.edmaputra.uwati.domain.tenancy.domain.Tenant} are updated.
 * <p>
 * Carries before-and-after snapshots of the settings within the core domain layer of the
 * Hexagonal Architecture, allowing downstream consumers to invalidate caches or reconfigure subsystems.
 *
 * @param tenantId the identifier of the tenant whose settings changed
 * @param previousSettings the previous list of tenant settings
 * @param updatedSettings the updated list of tenant settings
 * @param actor the identifier of the actor who performed the update
 * @param correlationId an optional correlation ID for request tracing across boundaries
 * @param occurredAt the timestamp when the update occurred
 * @author edmaputra
 * @since 0.0.1
 */
public record TenantSettingsUpdated(
		TenantId tenantId,
		List<TenantSetting> previousSettings,
		List<TenantSetting> updatedSettings,
		String actor,
		String correlationId,
		Instant occurredAt) {

	/**
	 * Compact constructor validating invariant constraints for the tenant settings updated event.
	 *
	 * @param tenantId the tenant ID
	 * @param previousSettings the previous settings list
	 * @param updatedSettings the updated settings list
	 * @param actor the initiating actor
	 * @param correlationId the correlation ID
	 * @param occurredAt the event timestamp
	 * @throws NullPointerException if {@code tenantId}, {@code previousSettings}, {@code updatedSettings},
	 *                              {@code actor}, or {@code occurredAt} is null
	 */
	public TenantSettingsUpdated {
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		Objects.requireNonNull(previousSettings, "Previous settings must not be null.");
		Objects.requireNonNull(updatedSettings, "Updated settings must not be null.");
		Objects.requireNonNull(actor, "Actor must not be null.");
		Objects.requireNonNull(occurredAt, "Occurred-at timestamp must not be null.");
	}

	/**
	 * Factory method creating a {@link TenantSettingsUpdated} event with defensive copies and the current timestamp.
	 *
	 * @param tenantId the tenant ID
	 * @param previousSettings the previous settings
	 * @param updatedSettings the updated settings
	 * @param actor the initiating actor
	 * @param correlationId an optional correlation ID for request tracing
	 * @return a new {@code TenantSettingsUpdated} instance
	 */
	public static TenantSettingsUpdated of(TenantId tenantId, List<TenantSetting> previousSettings,
			List<TenantSetting> updatedSettings, String actor, String correlationId) {
		return new TenantSettingsUpdated(tenantId, List.copyOf(previousSettings),
				List.copyOf(updatedSettings), actor, correlationId, Instant.now());
	}
}
