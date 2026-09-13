package io.github.edmaputra.uwati.domain.tenancy.domain.event;

import java.time.Instant;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.tenancy.domain.Tenant;

/**
 * Domain event published when a new {@link Tenant} is successfully created.
 * <p>
 * Emitted by the core domain layer in the Hexagonal Architecture to notify other
 * modules and downstream adapters of tenant registration.
 *
 * @param tenant the newly created tenant entity
 * @param actor the identifier of the actor who initiated the creation
 * @param correlationId an optional correlation ID for request tracing across boundaries
 * @param occurredAt the point in time when the event occurred
 * @author edmaputra
 * @since 0.0.1
 */
public record TenantCreated(Tenant tenant, String actor, String correlationId, Instant occurredAt) {

	/**
	 * Compact constructor validating invariant constraints for the tenant created event.
	 *
	 * @param tenant the created tenant
	 * @param actor the initiating actor
	 * @param correlationId the correlation ID
	 * @param occurredAt the timestamp when the event occurred
	 * @throws NullPointerException if {@code tenant}, {@code actor}, or {@code occurredAt} is null
	 */
	public TenantCreated {
		Objects.requireNonNull(tenant, "Tenant must not be null.");
		Objects.requireNonNull(actor, "Actor must not be null.");
		Objects.requireNonNull(occurredAt, "Occurred-at timestamp must not be null.");
	}

	/**
	 * Factory method creating a {@link TenantCreated} event with the current timestamp.
	 *
	 * @param tenant the created tenant
	 * @param actor the initiating actor
	 * @param correlationId an optional correlation ID for request tracing
	 * @return a new {@code TenantCreated} instance
	 */
	public static TenantCreated of(Tenant tenant, String actor, String correlationId) {
		return new TenantCreated(tenant, actor, correlationId, Instant.now());
	}
}
