package io.github.edmaputra.uwati.iam.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import lombok.Getter;

@Getter
public class Role {

	private final RoleId id;
	private final TenantId tenantId;
	private final String code;
	private String name;
	private String description;
	private final boolean systemRole;
	private Set<String> permissions;
	private final Instant createdAt;
	private Instant updatedAt;

	public Role(
			RoleId id,
			TenantId tenantId,
			String code,
			String name,
			String description,
			boolean systemRole,
			Set<String> permissions,
			Instant createdAt,
			Instant updatedAt) {
		this.id = Objects.requireNonNull(id, "Role ID must not be null.");
		this.tenantId = tenantId; // Nullable for global/system roles
		this.code = validateCode(code);
		this.name = validateName(name);
		this.description = description;
		this.systemRole = systemRole;
		this.permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
		this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt must not be null.");
		this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt must not be null.");
	}

	public static Role createCustom(
			TenantId tenantId,
			String code,
			String name,
			String description,
			Set<String> permissions) {
		Objects.requireNonNull(tenantId, "Tenant ID must not be null for custom roles.");
		Instant now = Instant.now();
		return new Role(
				RoleId.generate(),
				tenantId,
				code,
				name,
				description,
				false,
				permissions,
				now,
				now);
	}

	public static Role createSystemRole(
			String code,
			String name,
			String description,
			Set<String> permissions) {
		Instant now = Instant.now();
		return new Role(
				RoleId.generate(),
				null,
				code,
				name,
				description,
				true,
				permissions,
				now,
				now);
	}

	public void update(String name, String description, Set<String> permissions) {
		if (this.systemRole) {
			throw new IllegalStateException("System role '" + this.code + "' is immutable and cannot be updated.");
		}
		this.name = validateName(name);
		this.description = description;
		this.permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
		this.updatedAt = Instant.now();
	}

	public Optional<TenantId> optionalTenantId() {
		return Optional.ofNullable(tenantId);
	}

	public Optional<String> optionalDescription() {
		return Optional.ofNullable(description);
	}

	public Set<String> permissions() {
		return Collections.unmodifiableSet(permissions);
	}

	public boolean hasPermission(String permission) {
		return permission != null && permissions.contains(permission);
	}

	private static String validateCode(String code) {
		Objects.requireNonNull(code, "Role code must not be null.");
		String trimmed = code.trim().toUpperCase();
		if (trimmed.isBlank()) {
			throw new IllegalArgumentException("Role code must not be blank.");
		}
		return trimmed;
	}

	private static String validateName(String name) {
		Objects.requireNonNull(name, "Role name must not be null.");
		String trimmed = name.trim();
		if (trimmed.isBlank()) {
			throw new IllegalArgumentException("Role name must not be blank.");
		}
		return trimmed;
	}
}
