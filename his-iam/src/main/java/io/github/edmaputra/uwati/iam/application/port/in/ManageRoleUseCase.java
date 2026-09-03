package io.github.edmaputra.uwati.iam.application.port.in;

import java.util.List;

import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.domain.model.Role;
import io.github.edmaputra.uwati.iam.domain.model.RoleId;

/**
 * Inbound port for managing role catalog entries, custom role definitions, and permissions.
 *
 * @author edmaputra
 */
public interface ManageRoleUseCase {

	/**
	 * Creates a new custom role within a tenant.
	 *
	 * @param command the role creation command
	 * @param context the operation context
	 * @return the created {@link Role}
	 */
	Role createRole(CreateRoleCommand command, OperationContext context);

	/**
	 * Retrieves a role by ID.
	 *
	 * @param id the role ID
	 * @return the matching {@link Role}
	 */
	Role getRoleById(RoleId id);

	/**
	 * Updates a custom role's name, description, and permissions.
	 *
	 * @param command the update command
	 * @param context the operation context
	 * @return the updated {@link Role}
	 */
	Role updateRole(UpdateRoleCommand command, OperationContext context);

	/**
	 * Deletes a custom role.
	 *
	 * @param command the delete command
	 * @param context the operation context
	 */
	void deleteRole(DeleteRoleCommand command, OperationContext context);

	/**
	 * Lists roles available to a tenant (supports filtering by ALL, SYSTEM, or CUSTOM).
	 *
	 * @param tenantId   the tenant ID
	 * @param typeFilter optional filter ("ALL", "SYSTEM", "CUSTOM")
	 * @return list of matching {@link Role} entities
	 */
	List<Role> listRoles(TenantId tenantId, String typeFilter);

	/**
	 * Retrieves all registered system permissions.
	 *
	 * @return list of permission codes
	 */
	List<String> listAvailablePermissions();
}
