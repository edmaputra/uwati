package io.github.edmaputra.uwati.iam.adapter.rest;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.edmaputra.uwati.domain.security.CurrentActorProvider;
import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.adapter.rest.dto.CreateRoleRequest;
import io.github.edmaputra.uwati.iam.adapter.rest.dto.RoleResponse;
import io.github.edmaputra.uwati.iam.adapter.rest.dto.UpdateRoleRequest;
import io.github.edmaputra.uwati.iam.application.port.in.DeleteRoleCommand;
import io.github.edmaputra.uwati.iam.application.port.in.ManageRoleUseCase;
import io.github.edmaputra.uwati.iam.domain.model.Role;
import io.github.edmaputra.uwati.iam.domain.model.RoleId;
import jakarta.servlet.http.HttpServletRequest;

/**
 * REST controller exposing role management, permission catalogs, and custom role CRUD endpoints
 * under {@code /api/v1/iam/roles} and {@code /api/v1/iam/permissions}.
 *
 * @author edmaputra
 */
@RestController
@RequestMapping("/api/v1/iam")
public class RoleController {

	private final ManageRoleUseCase manageRoleUseCase;
	private final CurrentActorProvider currentActorProvider;

	/**
	 * Constructs the role controller.
	 *
	 * @param manageRoleUseCase    the role management inbound port
	 * @param currentActorProvider the current actor provider
	 */
	public RoleController(
			ManageRoleUseCase manageRoleUseCase,
			CurrentActorProvider currentActorProvider) {
		this.manageRoleUseCase = Objects.requireNonNull(manageRoleUseCase, "ManageRoleUseCase must not be null.");
		this.currentActorProvider = currentActorProvider;
	}

	/**
	 * Creates a new custom role.
	 *
	 * @param tenantId    optional tenant ID context
	 * @param request     the create role request
	 * @param httpRequest the servlet request
	 * @return HTTP 201 with created {@link RoleResponse}
	 */
	@PostMapping("/roles")
	public ResponseEntity<RoleResponse> createRole(
			@RequestParam(required = false) String tenantId,
			@RequestBody CreateRoleRequest request,
			HttpServletRequest httpRequest) {
		if (request == null) {
			throw new IllegalArgumentException("Request body must not be null.");
		}
		TenantId tenant = tenantId != null && !tenantId.isBlank() ? TenantId.from(tenantId) : null;
		OperationContext context = OperationContextHelper.resolveContext(httpRequest, currentActorProvider);
		Role role = manageRoleUseCase.createRole(request.toCommand(tenant), context);
		return ResponseEntity.status(HttpStatus.CREATED).body(RoleResponse.from(role));
	}

	/**
	 * Lists roles available to a tenant (supports filtering by ALL, SYSTEM, or CUSTOM).
	 *
	 * @param tenantId the tenant ID
	 * @param type     optional filter ("ALL", "SYSTEM", "CUSTOM")
	 * @return HTTP 200 with list of {@link RoleResponse}
	 */
	@GetMapping("/roles")
	public ResponseEntity<List<RoleResponse>> listRoles(
			@RequestParam String tenantId,
			@RequestParam(required = false) String type) {
		List<Role> roles = manageRoleUseCase.listRoles(TenantId.from(tenantId), type);
		return ResponseEntity.ok(roles.stream().map(RoleResponse::from).toList());
	}

	/**
	 * Retrieves role details by ID.
	 *
	 * @param id the role ID
	 * @return HTTP 200 with {@link RoleResponse}
	 */
	@GetMapping("/roles/{id}")
	public ResponseEntity<RoleResponse> getRole(@PathVariable String id) {
		Role role = manageRoleUseCase.getRoleById(RoleId.from(id));
		return ResponseEntity.ok(RoleResponse.from(role));
	}

	/**
	 * Updates a custom role's name, description, and permissions.
	 *
	 * @param id          the role ID
	 * @param tenantId    optional tenant ID
	 * @param request     the update role request
	 * @param httpRequest the servlet request
	 * @return HTTP 200 with updated {@link RoleResponse}
	 */
	@PutMapping("/roles/{id}")
	public ResponseEntity<RoleResponse> updateRole(
			@PathVariable String id,
			@RequestParam(required = false) String tenantId,
			@RequestBody UpdateRoleRequest request,
			HttpServletRequest httpRequest) {
		if (request == null) {
			throw new IllegalArgumentException("Request body must not be null.");
		}
		TenantId tenant = tenantId != null && !tenantId.isBlank() ? TenantId.from(tenantId) : null;
		OperationContext context = OperationContextHelper.resolveContext(httpRequest, currentActorProvider);
		Role role = manageRoleUseCase.updateRole(request.toCommand(tenant, RoleId.from(id)), context);
		return ResponseEntity.ok(RoleResponse.from(role));
	}

	/**
	 * Deletes a custom role.
	 *
	 * @param id          the role ID
	 * @param tenantId    optional tenant ID
	 * @param httpRequest the servlet request
	 * @return HTTP 204 No Content
	 */
	@DeleteMapping("/roles/{id}")
	public ResponseEntity<Void> deleteRole(
			@PathVariable String id,
			@RequestParam(required = false) String tenantId,
			HttpServletRequest httpRequest) {
		TenantId tenant = tenantId != null && !tenantId.isBlank() ? TenantId.from(tenantId) : null;
		OperationContext context = OperationContextHelper.resolveContext(httpRequest, currentActorProvider);
		manageRoleUseCase.deleteRole(new DeleteRoleCommand(tenant, RoleId.from(id)), context);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Lists all registered system permissions.
	 *
	 * @return HTTP 200 with list of permission codes
	 */
	@GetMapping("/permissions")
	public ResponseEntity<List<String>> listPermissions() {
		List<String> permissions = manageRoleUseCase.listAvailablePermissions();
		return ResponseEntity.ok(permissions);
	}
}
