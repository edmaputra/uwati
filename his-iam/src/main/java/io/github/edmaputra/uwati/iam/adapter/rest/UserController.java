package io.github.edmaputra.uwati.iam.adapter.rest;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
import io.github.edmaputra.uwati.iam.adapter.rest.dto.AssignUserRoleRequest;
import io.github.edmaputra.uwati.iam.adapter.rest.dto.ChangeUserStatusRequest;
import io.github.edmaputra.uwati.iam.adapter.rest.dto.CreateUserRequest;
import io.github.edmaputra.uwati.iam.adapter.rest.dto.EffectiveAccessResponse;
import io.github.edmaputra.uwati.iam.adapter.rest.dto.UpdatePasswordRequest;
import io.github.edmaputra.uwati.iam.adapter.rest.dto.UpdateUserProfileRequest;
import io.github.edmaputra.uwati.iam.adapter.rest.dto.UserDetailResponse;
import io.github.edmaputra.uwati.iam.adapter.rest.dto.UserIdentityResponse;
import io.github.edmaputra.uwati.iam.adapter.rest.dto.UserResponse;
import io.github.edmaputra.uwati.iam.adapter.rest.dto.UserRoleAssignmentResponse;
import io.github.edmaputra.uwati.iam.application.model.EffectiveAccess;
import io.github.edmaputra.uwati.iam.application.port.in.DeleteUserCommand;
import io.github.edmaputra.uwati.iam.application.port.in.ManageUserUseCase;
import io.github.edmaputra.uwati.iam.application.port.in.UserQuery;
import io.github.edmaputra.uwati.iam.domain.model.User;
import io.github.edmaputra.uwati.iam.domain.model.UserId;
import io.github.edmaputra.uwati.iam.domain.model.UserIdentity;
import io.github.edmaputra.uwati.iam.domain.model.UserIdentityId;
import io.github.edmaputra.uwati.iam.domain.model.UserRoleAssignment;
import io.github.edmaputra.uwati.iam.domain.model.UserRoleAssignmentId;
import io.github.edmaputra.uwati.iam.domain.model.UserStatus;
import jakarta.servlet.http.HttpServletRequest;

/**
 * REST controller exposing user management, lifecycle state changes, password resets,
 * identity links, and direct role assignment endpoints under {@code /api/v1/iam/users}.
 *
 * @author edmaputra
 */
@RestController
@RequestMapping("/api/v1/iam/users")
public class UserController {

	private final ManageUserUseCase manageUserUseCase;
	private final CurrentActorProvider currentActorProvider;

	/**
	 * Constructs the user controller.
	 *
	 * @param manageUserUseCase    the user management inbound port
	 * @param currentActorProvider the current actor provider
	 */
	public UserController(
			ManageUserUseCase manageUserUseCase,
			CurrentActorProvider currentActorProvider) {
		this.manageUserUseCase = Objects.requireNonNull(manageUserUseCase, "ManageUserUseCase must not be null.");
		this.currentActorProvider = currentActorProvider;
	}

	/**
	 * Provisions a new user account.
	 *
	 * @param request     the create user request payload
	 * @param httpRequest the servlet request
	 * @return HTTP 201 with {@link UserResponse}
	 */
	@PostMapping
	public ResponseEntity<UserResponse> createUser(
			@RequestBody CreateUserRequest request,
			HttpServletRequest httpRequest) {
		if (request == null) {
			throw new IllegalArgumentException("Request body must not be null.");
		}
		OperationContext context = OperationContextHelper.resolveContext(httpRequest, currentActorProvider);
		User user = manageUserUseCase.createUser(request.toCommand(), context);
		return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
	}

	/**
	 * Lists users matching optional search and status filter criteria.
	 *
	 * @param tenantId optional tenant ID
	 * @param search   optional search keyword
	 * @param status   optional status filter
	 * @return HTTP 200 with list of {@link UserResponse}
	 */
	@GetMapping
	public ResponseEntity<List<UserResponse>> listUsers(
			@RequestParam(required = false) String tenantId,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String status) {
		TenantId tenant = tenantId != null && !tenantId.isBlank() ? TenantId.from(tenantId) : null;
		UserStatus userStatus = status != null && !status.isBlank() ? UserStatus.valueOf(status.trim().toUpperCase()) : null;
		UserQuery query = new UserQuery(search, userStatus);

		List<User> users = manageUserUseCase.listUsers(tenant, query);
		return ResponseEntity.ok(users.stream().map(UserResponse::from).toList());
	}

	/**
	 * Retrieves complete user details including direct assignments and linked identities.
	 *
	 * @param id       the user ID
	 * @param tenantId optional tenant ID context for assignments
	 * @return HTTP 200 with {@link UserDetailResponse}
	 */
	@GetMapping("/{id}")
	public ResponseEntity<UserDetailResponse> getUser(
			@PathVariable String id,
			@RequestParam(required = false) String tenantId) {
		UserId userId = UserId.from(id);
		TenantId tenant = tenantId != null && !tenantId.isBlank() ? TenantId.from(tenantId) : null;

		User user = manageUserUseCase.getUserById(userId);
		List<UserRoleAssignment> assignments = manageUserUseCase.getUserRoleAssignments(userId, tenant);
		List<UserIdentity> identities = manageUserUseCase.getUserIdentities(userId);

		return ResponseEntity.ok(UserDetailResponse.of(user, assignments, identities));
	}

	/**
	 * Updates a user's full name.
	 *
	 * @param id          the user ID
	 * @param request     the profile update request
	 * @param httpRequest the servlet request
	 * @return HTTP 200 with updated {@link UserResponse}
	 */
	@PutMapping("/{id}")
	public ResponseEntity<UserResponse> updateProfile(
			@PathVariable String id,
			@RequestBody UpdateUserProfileRequest request,
			HttpServletRequest httpRequest) {
		if (request == null) {
			throw new IllegalArgumentException("Request body must not be null.");
		}
		OperationContext context = OperationContextHelper.resolveContext(httpRequest, currentActorProvider);
		User user = manageUserUseCase.updateUserProfile(request.toCommand(UserId.from(id)), context);
		return ResponseEntity.ok(UserResponse.from(user));
	}

	/**
	 * Changes a user's lifecycle status.
	 *
	 * @param id          the user ID
	 * @param request     the status change request
	 * @param httpRequest the servlet request
	 * @return HTTP 200 with updated {@link UserResponse}
	 */
	@PatchMapping("/{id}/status")
	public ResponseEntity<UserResponse> changeStatus(
			@PathVariable String id,
			@RequestBody ChangeUserStatusRequest request,
			HttpServletRequest httpRequest) {
		if (request == null) {
			throw new IllegalArgumentException("Request body must not be null.");
		}
		OperationContext context = OperationContextHelper.resolveContext(httpRequest, currentActorProvider);
		User user = manageUserUseCase.changeUserStatus(request.toCommand(UserId.from(id)), context);
		return ResponseEntity.ok(UserResponse.from(user));
	}

	/**
	 * Updates/resets a user's password.
	 *
	 * @param id          the user ID
	 * @param request     the update password request
	 * @param httpRequest the servlet request
	 * @return HTTP 204 No Content
	 */
	@PutMapping("/{id}/password")
	public ResponseEntity<Void> updatePassword(
			@PathVariable String id,
			@RequestBody UpdatePasswordRequest request,
			HttpServletRequest httpRequest) {
		if (request == null) {
			throw new IllegalArgumentException("Request body must not be null.");
		}
		OperationContext context = OperationContextHelper.resolveContext(httpRequest, currentActorProvider);
		manageUserUseCase.updatePassword(request.toCommand(UserId.from(id)), context);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Deletes/deactivates a user.
	 *
	 * @param id          the user ID
	 * @param httpRequest the servlet request
	 * @return HTTP 204 No Content
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(
			@PathVariable String id,
			HttpServletRequest httpRequest) {
		OperationContext context = OperationContextHelper.resolveContext(httpRequest, currentActorProvider);
		manageUserUseCase.deleteUser(new DeleteUserCommand(UserId.from(id)), context);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Calculates and returns the compiled effective access model for a user in a tenant.
	 *
	 * @param id       the user ID
	 * @param tenantId the tenant ID
	 * @return HTTP 200 with {@link EffectiveAccessResponse}
	 */
	@GetMapping("/{id}/effective-access")
	public ResponseEntity<EffectiveAccessResponse> getEffectiveAccess(
			@PathVariable String id,
			@RequestParam String tenantId) {
		UserId userId = UserId.from(id);
		TenantId tenant = TenantId.from(tenantId);
		EffectiveAccess access = manageUserUseCase.getUserEffectiveAccess(userId, tenant);
		return ResponseEntity.ok(EffectiveAccessResponse.from(userId, access));
	}

	/**
	 * Retrieves linked federated identities for a user.
	 *
	 * @param id the user ID
	 * @return HTTP 200 with list of {@link UserIdentityResponse}
	 */
	@GetMapping("/{id}/identities")
	public ResponseEntity<List<UserIdentityResponse>> getIdentities(@PathVariable String id) {
		List<UserIdentity> identities = manageUserUseCase.getUserIdentities(UserId.from(id));
		return ResponseEntity.ok(identities.stream().map(UserIdentityResponse::from).toList());
	}

	/**
	 * Unlinks a federated identity from a user.
	 *
	 * @param id          the user ID
	 * @param identityId  the identity ID
	 * @param httpRequest the servlet request
	 * @return HTTP 204 No Content
	 */
	@DeleteMapping("/{id}/identities/{identityId}")
	public ResponseEntity<Void> unlinkIdentity(
			@PathVariable String id,
			@PathVariable String identityId,
			HttpServletRequest httpRequest) {
		OperationContext context = OperationContextHelper.resolveContext(httpRequest, currentActorProvider);
		manageUserUseCase.unlinkUserIdentity(UserId.from(id), UserIdentityId.from(identityId), context);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Lists direct role assignments for a user.
	 *
	 * @param userId   the user ID
	 * @param tenantId optional tenant ID filter
	 * @return HTTP 200 with list of {@link UserRoleAssignmentResponse}
	 */
	@GetMapping("/{userId}/assignments")
	public ResponseEntity<List<UserRoleAssignmentResponse>> getAssignments(
			@PathVariable String userId,
			@RequestParam(required = false) String tenantId) {
		TenantId tenant = tenantId != null && !tenantId.isBlank() ? TenantId.from(tenantId) : null;
		List<UserRoleAssignment> assignments = manageUserUseCase.getUserRoleAssignments(UserId.from(userId), tenant);
		return ResponseEntity.ok(assignments.stream().map(UserRoleAssignmentResponse::from).toList());
	}

	/**
	 * Assigns a role to a user.
	 *
	 * @param userId      the user ID
	 * @param request     the assignment request
	 * @param httpRequest the servlet request
	 * @return HTTP 201 with created {@link UserRoleAssignmentResponse}
	 */
	@PostMapping("/{userId}/assignments")
	public ResponseEntity<UserRoleAssignmentResponse> assignRole(
			@PathVariable String userId,
			@RequestBody AssignUserRoleRequest request,
			HttpServletRequest httpRequest) {
		if (request == null) {
			throw new IllegalArgumentException("Request body must not be null.");
		}
		OperationContext context = OperationContextHelper.resolveContext(httpRequest, currentActorProvider);
		UserRoleAssignment assignment = manageUserUseCase.assignRoleToUser(request.toCommand(UserId.from(userId)), context);
		return ResponseEntity.status(HttpStatus.CREATED).body(UserRoleAssignmentResponse.from(assignment));
	}

	/**
	 * Revokes a direct role assignment from a user.
	 *
	 * @param userId       the user ID
	 * @param assignmentId the assignment ID
	 * @param httpRequest  the servlet request
	 * @return HTTP 204 No Content
	 */
	@DeleteMapping("/{userId}/assignments/{assignmentId}")
	public ResponseEntity<Void> revokeAssignment(
			@PathVariable String userId,
			@PathVariable String assignmentId,
			HttpServletRequest httpRequest) {
		OperationContext context = OperationContextHelper.resolveContext(httpRequest, currentActorProvider);
		manageUserUseCase.revokeUserRoleAssignment(UserRoleAssignmentId.from(assignmentId), context);
		return ResponseEntity.noContent().build();
	}
}
