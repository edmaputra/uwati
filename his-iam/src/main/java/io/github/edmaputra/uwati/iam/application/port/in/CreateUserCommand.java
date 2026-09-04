package io.github.edmaputra.uwati.iam.application.port.in;

import java.util.Objects;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.domain.model.GroupId;
import io.github.edmaputra.uwati.iam.domain.model.RoleId;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;

/**
 * Command for provisioning a new user account with optional initial role and group assignments.
 *
 * @param email              the unique email address
 * @param rawPassword        optional raw password (nullable for SSO-only users)
 * @param fullName           the full name of the user
 * @param platformSuperAdmin whether this user has global superadmin privileges
 * @param tenantId           optional initial tenant ID context
 * @param roleId             optional initial role ID to assign
 * @param scopeNodeId        optional initial scope node ID
 * @param inheritChildren    whether role permissions cascade downward to descendant scopes
 * @param groupId            optional initial group ID to join
 * @author edmaputra
 */
public record CreateUserCommand(
		String email,
		String rawPassword,
		String fullName,
		boolean platformSuperAdmin,
		TenantId tenantId,
		RoleId roleId,
		ScopeNodeId scopeNodeId,
		boolean inheritChildren,
		GroupId groupId) {

	public CreateUserCommand {
		Objects.requireNonNull(email, "Email must not be null.");
		if (email.isBlank() || !email.contains("@")) {
			throw new IllegalArgumentException("Invalid email format: " + email);
		}
		Objects.requireNonNull(fullName, "Full name must not be null.");
		if (fullName.isBlank()) {
			throw new IllegalArgumentException("Full name must not be blank.");
		}
	}
}
