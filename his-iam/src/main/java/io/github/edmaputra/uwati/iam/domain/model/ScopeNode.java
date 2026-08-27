package io.github.edmaputra.uwati.iam.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantOwned;
import lombok.Getter;

@Getter
public class ScopeNode implements TenantOwned {

	private final ScopeNodeId id;
	private final TenantId tenantId;
	private ScopeNodeId parentId;
	private String code;
	private String name;
	private String path;
	private final Instant createdAt;
	private Instant updatedAt;

	public ScopeNode(
			ScopeNodeId id,
			TenantId tenantId,
			ScopeNodeId parentId,
			String code,
			String name,
			String path,
			Instant createdAt,
			Instant updatedAt) {
		this.id = Objects.requireNonNull(id, "ScopeNode ID must not be null.");
		this.tenantId = Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		this.parentId = parentId; // Nullable for root nodes
		this.code = validateCode(code);
		this.name = validateName(name);
		this.path = validatePath(path);
		this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt must not be null.");
		this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt must not be null.");
	}

	public static ScopeNode createRoot(
			TenantId tenantId,
			String code,
			String name) {
		ScopeNodeId nodeId = ScopeNodeId.generate();
		String path = "/" + tenantId.value() + "/" + nodeId.value() + "/";
		Instant now = Instant.now();
		return new ScopeNode(
				nodeId,
				tenantId,
				null,
				code,
				name,
				path,
				now,
				now);
	}

	public static ScopeNode createChild(
			TenantId tenantId,
			ScopeNode parent,
			String code,
			String name) {
		Objects.requireNonNull(parent, "Parent ScopeNode must not be null.");
		ScopeNodeId nodeId = ScopeNodeId.generate();
		String path = parent.getPath() + nodeId.value() + "/";
		Instant now = Instant.now();
		return new ScopeNode(
				nodeId,
				tenantId,
				parent.getId(),
				code,
				name,
				path,
				now,
				now);
	}

	@Override
	public TenantId tenantId() {
		return this.tenantId;
	}

	public void updateMetadata(String code, String name) {
		this.code = validateCode(code);
		this.name = validateName(name);
		this.updatedAt = Instant.now();
	}

	public void moveTo(ScopeNodeId newParentId, String newPath) {
		this.parentId = newParentId;
		this.path = validatePath(newPath);
		this.updatedAt = Instant.now();
	}

	public Optional<ScopeNodeId> optionalParentId() {
		return Optional.ofNullable(parentId);
	}

	public boolean isRoot() {
		return this.parentId == null;
	}

	private static String validateCode(String code) {
		Objects.requireNonNull(code, "ScopeNode code must not be null.");
		String trimmed = code.trim().toUpperCase();
		if (trimmed.isBlank()) {
			throw new IllegalArgumentException("ScopeNode code must not be blank.");
		}
		return trimmed;
	}

	private static String validateName(String name) {
		Objects.requireNonNull(name, "ScopeNode name must not be null.");
		String trimmed = name.trim();
		if (trimmed.isBlank()) {
			throw new IllegalArgumentException("ScopeNode name must not be blank.");
		}
		return trimmed;
	}

	private static String validatePath(String path) {
		Objects.requireNonNull(path, "ScopeNode path must not be null.");
		String trimmed = path.trim();
		if (trimmed.isBlank() || !trimmed.startsWith("/") || !trimmed.endsWith("/")) {
			throw new IllegalArgumentException("ScopeNode path must start and end with '/': " + path);
		}
		return trimmed;
	}
}
