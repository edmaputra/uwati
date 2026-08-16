package io.github.edmaputra.uwati.core.tenancy.domain;

import java.time.Instant;
import java.util.Objects;

public record Tenant(
		TenantId id,
		String legalName,
		String displayName,
		TenantStatus status,
		Instant createdAt,
		Instant updatedAt) {

	public Tenant {
		Objects.requireNonNull(id, "Tenant ID must not be null.");
		if (legalName == null || legalName.isBlank()) {
			throw new IllegalArgumentException("Tenant legal name must not be blank.");
		}
		if (displayName == null || displayName.isBlank()) {
			throw new IllegalArgumentException("Tenant display name must not be blank.");
		}
		Objects.requireNonNull(status, "Tenant status must not be null.");
		Objects.requireNonNull(createdAt, "Tenant creation timestamp must not be null.");
		Objects.requireNonNull(updatedAt, "Tenant update timestamp must not be null.");
		if (updatedAt.isBefore(createdAt)) {
			throw new IllegalArgumentException("Tenant update timestamp must not precede its creation timestamp.");
		}
	}

	public boolean isActive() {
		return status == TenantStatus.ACTIVE;
	}
}
