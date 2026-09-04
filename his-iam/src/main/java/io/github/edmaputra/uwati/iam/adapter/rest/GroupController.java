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
import io.github.edmaputra.uwati.iam.adapter.rest.dto.AddGroupMemberRequest;
import io.github.edmaputra.uwati.iam.adapter.rest.dto.AssignGroupRoleRequest;
import io.github.edmaputra.uwati.iam.adapter.rest.dto.CreateGroupRequest;
import io.github.edmaputra.uwati.iam.adapter.rest.dto.GroupResponse;
import io.github.edmaputra.uwati.iam.adapter.rest.dto.GroupRoleAssignmentResponse;
import io.github.edmaputra.uwati.iam.adapter.rest.dto.UpdateGroupRequest;
import io.github.edmaputra.uwati.iam.adapter.rest.dto.UserResponse;
import io.github.edmaputra.uwati.iam.application.port.in.DeleteGroupCommand;
import io.github.edmaputra.uwati.iam.application.port.in.ManageGroupUseCase;
import io.github.edmaputra.uwati.iam.application.port.in.RemoveGroupMemberCommand;
import io.github.edmaputra.uwati.iam.domain.model.Group;
import io.github.edmaputra.uwati.iam.domain.model.GroupId;
import io.github.edmaputra.uwati.iam.domain.model.GroupRoleAssignment;
import io.github.edmaputra.uwati.iam.domain.model.GroupRoleAssignmentId;
import io.github.edmaputra.uwati.iam.domain.model.User;
import io.github.edmaputra.uwati.iam.domain.model.UserId;
import jakarta.servlet.http.HttpServletRequest;

/**
 * REST controller exposing user group management, team rosters, and group-level
 * role assignment endpoints under {@code /api/v1/iam/groups}.
 *
 * @author edmaputra
 */
@RestController
@RequestMapping("/api/v1/iam/groups")
public class GroupController {

	private final ManageGroupUseCase manageGroupUseCase;
	private final CurrentActorProvider currentActorProvider;

	/**
	 * Constructs the group controller.
	 *
	 * @param manageGroupUseCase    the group management inbound port
	 * @param currentActorProvider the current actor provider
	 */
	public GroupController(
			ManageGroupUseCase manageGroupUseCase,
			CurrentActorProvider currentActorProvider) {
		this.manageGroupUseCase = Objects.requireNonNull(manageGroupUseCase, "ManageGroupUseCase must not be null.");
		this.currentActorProvider = currentActorProvider;
	}

	/**
	 * Creates a new group within a tenant.
	 *
	 * @param tenantId    the owning tenant ID
	 * @param request     the create group request
	 * @param httpRequest the servlet request
	 * @return HTTP 201 with created {@link GroupResponse}
	 */
	@PostMapping
	public ResponseEntity<GroupResponse> createGroup(
			@RequestParam String tenantId,
			@RequestBody CreateGroupRequest request,
			HttpServletRequest httpRequest) {
		if (request == null) {
			throw new IllegalArgumentException("Request body must not be null.");
		}
		OperationContext context = OperationContextHelper.resolveContext(httpRequest, currentActorProvider);
		Group group = manageGroupUseCase.createGroup(request.toCommand(TenantId.from(tenantId)), context);
		return ResponseEntity.status(HttpStatus.CREATED).body(GroupResponse.from(group));
	}

	/**
	 * Lists all groups for a tenant.
	 *
	 * @param tenantId the tenant ID
	 * @return HTTP 200 with list of {@link GroupResponse}
	 */
	@GetMapping
	public ResponseEntity<List<GroupResponse>> listGroups(@RequestParam String tenantId) {
		List<Group> groups = manageGroupUseCase.listGroups(TenantId.from(tenantId));
		return ResponseEntity.ok(groups.stream().map(GroupResponse::from).toList());
	}

	/**
	 * Retrieves group details by ID.
	 *
	 * @param id       the group ID
	 * @param tenantId the tenant ID
	 * @return HTTP 200 with {@link GroupResponse}
	 */
	@GetMapping("/{id}")
	public ResponseEntity<GroupResponse> getGroup(
			@PathVariable String id,
			@RequestParam String tenantId) {
		Group group = manageGroupUseCase.getGroupById(TenantId.from(tenantId), GroupId.from(id));
		return ResponseEntity.ok(GroupResponse.from(group));
	}

	/**
	 * Updates a group's metadata and SSO mapping.
	 *
	 * @param id          the group ID
	 * @param tenantId    the tenant ID
	 * @param request     the update request
	 * @param httpRequest the servlet request
	 * @return HTTP 200 with updated {@link GroupResponse}
	 */
	@PutMapping("/{id}")
	public ResponseEntity<GroupResponse> updateGroup(
			@PathVariable String id,
			@RequestParam String tenantId,
			@RequestBody UpdateGroupRequest request,
			HttpServletRequest httpRequest) {
		if (request == null) {
			throw new IllegalArgumentException("Request body must not be null.");
		}
		OperationContext context = OperationContextHelper.resolveContext(httpRequest, currentActorProvider);
		Group group = manageGroupUseCase.updateGroup(request.toCommand(TenantId.from(tenantId), GroupId.from(id)), context);
		return ResponseEntity.ok(GroupResponse.from(group));
	}

	/**
	 * Deletes a group and removes associated memberships and group assignments.
	 *
	 * @param id          the group ID
	 * @param tenantId    the tenant ID
	 * @param httpRequest the servlet request
	 * @return HTTP 204 No Content
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteGroup(
			@PathVariable String id,
			@RequestParam String tenantId,
			HttpServletRequest httpRequest) {
		OperationContext context = OperationContextHelper.resolveContext(httpRequest, currentActorProvider);
		manageGroupUseCase.deleteGroup(new DeleteGroupCommand(TenantId.from(tenantId), GroupId.from(id)), context);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Retrieves all member users belonging to a group.
	 *
	 * @param id       the group ID
	 * @param tenantId the tenant ID
	 * @return HTTP 200 with list of {@link UserResponse}
	 */
	@GetMapping("/{id}/members")
	public ResponseEntity<List<UserResponse>> getMembers(
			@PathVariable String id,
			@RequestParam String tenantId) {
		List<User> members = manageGroupUseCase.getGroupMembers(TenantId.from(tenantId), GroupId.from(id));
		return ResponseEntity.ok(members.stream().map(UserResponse::from).toList());
	}

	/**
	 * Adds a user to a group.
	 *
	 * @param id          the group ID
	 * @param tenantId    the tenant ID
	 * @param request     the add member request
	 * @param httpRequest the servlet request
	 * @return HTTP 204 No Content
	 */
	@PostMapping("/{id}/members")
	public ResponseEntity<Void> addMember(
			@PathVariable String id,
			@RequestParam String tenantId,
			@RequestBody AddGroupMemberRequest request,
			HttpServletRequest httpRequest) {
		if (request == null) {
			throw new IllegalArgumentException("Request body must not be null.");
		}
		OperationContext context = OperationContextHelper.resolveContext(httpRequest, currentActorProvider);
		manageGroupUseCase.addGroupMember(request.toCommand(TenantId.from(tenantId), GroupId.from(id)), context);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Removes a user from a group.
	 *
	 * @param id          the group ID
	 * @param userId      the user ID to remove
	 * @param tenantId    the tenant ID
	 * @param httpRequest the servlet request
	 * @return HTTP 204 No Content
	 */
	@DeleteMapping("/{id}/members/{userId}")
	public ResponseEntity<Void> removeMember(
			@PathVariable String id,
			@PathVariable String userId,
			@RequestParam String tenantId,
			HttpServletRequest httpRequest) {
		OperationContext context = OperationContextHelper.resolveContext(httpRequest, currentActorProvider);
		manageGroupUseCase.removeGroupMember(
				new RemoveGroupMemberCommand(TenantId.from(tenantId), GroupId.from(id), UserId.from(userId)),
				context);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Retrieves all role assignments bound to a group.
	 *
	 * @param id       the group ID
	 * @param tenantId the tenant ID
	 * @return HTTP 200 with list of {@link GroupRoleAssignmentResponse}
	 */
	@GetMapping("/{id}/assignments")
	public ResponseEntity<List<GroupRoleAssignmentResponse>> getAssignments(
			@PathVariable String id,
			@RequestParam String tenantId) {
		List<GroupRoleAssignment> assignments = manageGroupUseCase.getGroupRoleAssignments(TenantId.from(tenantId), GroupId.from(id));
		return ResponseEntity.ok(assignments.stream().map(GroupRoleAssignmentResponse::from).toList());
	}

	/**
	 * Assigns a role to a group at a tenant or scope node.
	 *
	 * @param id          the group ID
	 * @param tenantId    the tenant ID
	 * @param request     the assignment request
	 * @param httpRequest the servlet request
	 * @return HTTP 201 with created {@link GroupRoleAssignmentResponse}
	 */
	@PostMapping("/{id}/assignments")
	public ResponseEntity<GroupRoleAssignmentResponse> assignRole(
			@PathVariable String id,
			@RequestParam String tenantId,
			@RequestBody AssignGroupRoleRequest request,
			HttpServletRequest httpRequest) {
		if (request == null) {
			throw new IllegalArgumentException("Request body must not be null.");
		}
		OperationContext context = OperationContextHelper.resolveContext(httpRequest, currentActorProvider);
		GroupRoleAssignment assignment = manageGroupUseCase.assignRoleToGroup(
				request.toCommand(TenantId.from(tenantId), GroupId.from(id)), context);
		return ResponseEntity.status(HttpStatus.CREATED).body(GroupRoleAssignmentResponse.from(assignment));
	}

	/**
	 * Revokes a group role assignment.
	 *
	 * @param id           the group ID
	 * @param assignmentId the assignment ID to revoke
	 * @param httpRequest  the servlet request
	 * @return HTTP 204 No Content
	 */
	@DeleteMapping("/{id}/assignments/{assignmentId}")
	public ResponseEntity<Void> revokeAssignment(
			@PathVariable String id,
			@PathVariable String assignmentId,
			HttpServletRequest httpRequest) {
		OperationContext context = OperationContextHelper.resolveContext(httpRequest, currentActorProvider);
		manageGroupUseCase.revokeGroupRoleAssignment(GroupRoleAssignmentId.from(assignmentId), context);
		return ResponseEntity.noContent().build();
	}
}
