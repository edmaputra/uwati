package io.github.edmaputra.uwati.iam.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantOwned;
import lombok.Getter;

@Getter
public class GroupRoleAssignment implements TenantOwned {

	private final GroupRoleAssignmentId id;
	private final GroupId groupId;
	private final RoleId roleId;
	private final TenantId tenantId;
	private final ScopeNodeId scopeNodeId;
	private final boolean inheritChildren;
	private final Instant createdAt;

	public GroupRoleAssignment(
			GroupRoleAssignmentId id,
			GroupId groupId,
			RoleId roleId,
			TenantId tenantId,
			ScopeNodeId scopeNodeId,
			boolean inheritChildren,
			Instant createdAt) {
		this.id = Objects.requireNonNull(id, "Assignment ID must not be null.");
		this.groupId = Objects.requireNonNull(groupId, "Group ID must not be null.");
		this.roleId = Objects.requireNonNull(roleId, "Role ID must not be null.");
		this.tenantId = Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		this.scopeNodeId = scopeNodeId; // Nullable for tenant-wide assignments
		this.inheritChildren = inheritChildren;
		this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt must not be null.");
	}

	public static GroupRoleAssignment create(
			GroupId groupId,
			RoleId roleId,
			TenantId tenantId,
			ScopeNodeId scopeNodeId,
			boolean inheritChildren) {
		return new GroupRoleAssignment(
				GroupRoleAssignmentId.generate(),
				groupId,
				roleId,
				tenantId,
				scopeNodeId,
				inheritChildren,
				Instant.now());
	}

	public static GroupRoleAssignment createTenantWide(
			GroupId groupId,
			RoleId roleId,
			TenantId tenantId) {
		return create(groupId, roleId, tenantId, null, true);
	}

	@Override
	public TenantId tenantId() {
		return this.tenantId;
	}

	public Optional<ScopeNodeId> optionalScopeNodeId() {
		return Optional.ofNullable(scopeNodeId);
	}

	public boolean isTenantWide() {
		return this.scopeNodeId == null;
	}
}
