package io.github.edmaputra.uwati.iam.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import lombok.Getter;

@Getter
public class UserRoleAssignment {

	private final UserRoleAssignmentId id;
	private final UserId userId;
	private final RoleId roleId;
	private final TenantId tenantId;
	private final ScopeNodeId scopeNodeId;
	private final boolean inheritChildren;
	private final Instant createdAt;

	public UserRoleAssignment(
			UserRoleAssignmentId id,
			UserId userId,
			RoleId roleId,
			TenantId tenantId,
			ScopeNodeId scopeNodeId,
			boolean inheritChildren,
			Instant createdAt) {
		this.id = Objects.requireNonNull(id, "Assignment ID must not be null.");
		this.userId = Objects.requireNonNull(userId, "User ID must not be null.");
		this.roleId = Objects.requireNonNull(roleId, "Role ID must not be null.");
		this.tenantId = tenantId; // Nullable for global superadmin assignments
		this.scopeNodeId = scopeNodeId; // Nullable for tenant-wide assignments
		this.inheritChildren = inheritChildren;
		this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt must not be null.");
	}

	public static UserRoleAssignment create(
			UserId userId,
			RoleId roleId,
			TenantId tenantId,
			ScopeNodeId scopeNodeId,
			boolean inheritChildren) {
		return new UserRoleAssignment(
				UserRoleAssignmentId.generate(),
				userId,
				roleId,
				tenantId,
				scopeNodeId,
				inheritChildren,
				Instant.now());
	}

	public static UserRoleAssignment createTenantWide(
			UserId userId,
			RoleId roleId,
			TenantId tenantId) {
		return create(userId, roleId, tenantId, null, true);
	}

	public static UserRoleAssignment createGlobalSuperadmin(
			UserId userId,
			RoleId roleId) {
		return create(userId, roleId, null, null, true);
	}

	public Optional<TenantId> optionalTenantId() {
		return Optional.ofNullable(tenantId);
	}

	public Optional<ScopeNodeId> optionalScopeNodeId() {
		return Optional.ofNullable(scopeNodeId);
	}

	public boolean isTenantWide() {
		return this.scopeNodeId == null;
	}

	public boolean isGlobal() {
		return this.tenantId == null;
	}
}
