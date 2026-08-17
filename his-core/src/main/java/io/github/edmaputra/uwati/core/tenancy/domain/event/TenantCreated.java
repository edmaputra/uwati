package io.github.edmaputra.uwati.core.tenancy.domain.event;

import java.time.Instant;
import java.util.Objects;

import io.github.edmaputra.uwati.core.tenancy.domain.Tenant;

public record TenantCreated(Tenant tenant, Instant occurredAt) {

	public TenantCreated {
		Objects.requireNonNull(tenant, "Tenant must not be null.");
		Objects.requireNonNull(occurredAt, "Occurred-at timestamp must not be null.");
	}

	public static TenantCreated of(Tenant tenant) {
		return new TenantCreated(tenant, Instant.now());
	}
}
