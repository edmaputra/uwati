package io.github.edmaputra.uwati.domain.tenancy.domain.event;

import java.time.Instant;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.tenancy.domain.Tenant;

public record TenantCreated(Tenant tenant, String actor, String correlationId, Instant occurredAt) {

	public TenantCreated {
		Objects.requireNonNull(tenant, "Tenant must not be null.");
		Objects.requireNonNull(actor, "Actor must not be null.");
		Objects.requireNonNull(occurredAt, "Occurred-at timestamp must not be null.");
	}

	public static TenantCreated of(Tenant tenant, String actor, String correlationId) {
		return new TenantCreated(tenant, actor, correlationId, Instant.now());
	}
}
