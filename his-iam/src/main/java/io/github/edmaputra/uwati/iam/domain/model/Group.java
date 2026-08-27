package io.github.edmaputra.uwati.iam.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantOwned;
import lombok.Getter;

@Getter
public class Group implements TenantOwned {

	private final GroupId id;
	private final TenantId tenantId;
	private final String code;
	private String name;
	private String description;
	private String externalIdpGroupName;
	private final Instant createdAt;
	private Instant updatedAt;

	public Group(
			GroupId id,
			TenantId tenantId,
			String code,
			String name,
			String description,
			String externalIdpGroupName,
			Instant createdAt,
			Instant updatedAt) {
		this.id = Objects.requireNonNull(id, "Group ID must not be null.");
		this.tenantId = Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		this.code = validateCode(code);
		this.name = validateName(name);
		this.description = description;
		this.externalIdpGroupName = externalIdpGroupName;
		this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt must not be null.");
		this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt must not be null.");
	}

	public static Group create(
			TenantId tenantId,
			String code,
			String name,
			String description,
			String externalIdpGroupName) {
		Instant now = Instant.now();
		return new Group(
				GroupId.generate(),
				tenantId,
				code,
				name,
				description,
				externalIdpGroupName,
				now,
				now);
	}

	@Override
	public TenantId tenantId() {
		return this.tenantId;
	}

	public void updateDetails(String name, String description, String externalIdpGroupName) {
		this.name = validateName(name);
		this.description = description;
		this.externalIdpGroupName = externalIdpGroupName;
		this.updatedAt = Instant.now();
	}

	public Optional<String> optionalDescription() {
		return Optional.ofNullable(description);
	}

	public Optional<String> optionalExternalIdpGroupName() {
		return Optional.ofNullable(externalIdpGroupName);
	}

	private static String validateCode(String code) {
		Objects.requireNonNull(code, "Group code must not be null.");
		String trimmed = code.trim().toUpperCase();
		if (trimmed.isBlank()) {
			throw new IllegalArgumentException("Group code must not be blank.");
		}
		return trimmed;
	}

	private static String validateName(String name) {
		Objects.requireNonNull(name, "Group name must not be null.");
		String trimmed = name.trim();
		if (trimmed.isBlank()) {
			throw new IllegalArgumentException("Group name must not be blank.");
		}
		return trimmed;
	}
}
