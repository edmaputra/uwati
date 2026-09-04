package io.github.edmaputra.uwati.iam.application.service;

import java.util.List;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.application.port.in.AddGroupMemberCommand;
import io.github.edmaputra.uwati.iam.application.port.in.AssignGroupRoleCommand;
import io.github.edmaputra.uwati.iam.application.port.in.CreateGroupCommand;
import io.github.edmaputra.uwati.iam.application.port.in.DeleteGroupCommand;
import io.github.edmaputra.uwati.iam.application.port.in.ManageGroupUseCase;
import io.github.edmaputra.uwati.iam.application.port.in.RemoveGroupMemberCommand;
import io.github.edmaputra.uwati.iam.application.port.in.UpdateGroupCommand;
import io.github.edmaputra.uwati.iam.domain.event.IamEvent;
import io.github.edmaputra.uwati.iam.domain.event.IamEventTypes;
import io.github.edmaputra.uwati.iam.domain.exception.GroupNotFoundException;
import io.github.edmaputra.uwati.iam.domain.exception.RoleNotFoundException;
import io.github.edmaputra.uwati.iam.domain.exception.ScopeNodeNotFoundException;
import io.github.edmaputra.uwati.iam.domain.exception.UserNotFoundException;
import io.github.edmaputra.uwati.iam.domain.model.Group;
import io.github.edmaputra.uwati.iam.domain.model.GroupId;
import io.github.edmaputra.uwati.iam.domain.model.GroupRoleAssignment;
import io.github.edmaputra.uwati.iam.domain.model.GroupRoleAssignmentId;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNode;
import io.github.edmaputra.uwati.iam.domain.model.User;
import io.github.edmaputra.uwati.iam.domain.model.UserGroupMembership;
import io.github.edmaputra.uwati.iam.domain.model.UserId;
import io.github.edmaputra.uwati.iam.domain.repository.GroupRepository;
import io.github.edmaputra.uwati.iam.domain.repository.GroupRoleAssignmentRepository;
import io.github.edmaputra.uwati.iam.domain.repository.RoleRepository;
import io.github.edmaputra.uwati.iam.domain.repository.ScopeNodeRepository;
import io.github.edmaputra.uwati.iam.domain.repository.UserGroupMembershipRepository;
import io.github.edmaputra.uwati.iam.domain.repository.UserRepository;

/**
 * Application service implementing {@link ManageGroupUseCase}.
 * Manages user groups, team membership rosters, external IdP group mappings,
 * and group-level role-to-scope bindings.
 *
 * @author edmaputra
 */
public class GroupService implements ManageGroupUseCase {

	private final GroupRepository groupRepository;
	private final UserGroupMembershipRepository userGroupMembershipRepository;
	private final GroupRoleAssignmentRepository groupRoleAssignmentRepository;
	private final RoleRepository roleRepository;
	private final ScopeNodeRepository scopeNodeRepository;
	private final UserRepository userRepository;
	private final ApplicationEventPublisher eventPublisher;

	/**
	 * Constructs the group service with all required dependencies.
	 *
	 * @param groupRepository               the group repository
	 * @param userGroupMembershipRepository the user group membership repository
	 * @param groupRoleAssignmentRepository the group role assignment repository
	 * @param roleRepository               the role repository
	 * @param scopeNodeRepository          the scope node repository
	 * @param userRepository               the user repository
	 * @param eventPublisher               the application event publisher
	 */
	public GroupService(
			GroupRepository groupRepository,
			UserGroupMembershipRepository userGroupMembershipRepository,
			GroupRoleAssignmentRepository groupRoleAssignmentRepository,
			RoleRepository roleRepository,
			ScopeNodeRepository scopeNodeRepository,
			UserRepository userRepository,
			ApplicationEventPublisher eventPublisher) {
		this.groupRepository = Objects.requireNonNull(groupRepository, "GroupRepository must not be null.");
		this.userGroupMembershipRepository = Objects.requireNonNull(userGroupMembershipRepository, "UserGroupMembershipRepository must not be null.");
		this.groupRoleAssignmentRepository = Objects.requireNonNull(groupRoleAssignmentRepository, "GroupRoleAssignmentRepository must not be null.");
		this.roleRepository = Objects.requireNonNull(roleRepository, "RoleRepository must not be null.");
		this.scopeNodeRepository = Objects.requireNonNull(scopeNodeRepository, "ScopeNodeRepository must not be null.");
		this.userRepository = Objects.requireNonNull(userRepository, "UserRepository must not be null.");
		this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher must not be null.");
	}

	@Override
	@Transactional
	public Group createGroup(CreateGroupCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Command must not be null.");

		if (groupRepository.existsByTenantIdAndCode(command.tenantId(), command.code())) {
			throw new IllegalArgumentException("A group with code '" + command.code() + "' already exists in this tenant.");
		}

		Group group = Group.create(
				command.tenantId(),
				command.code(),
				command.name(),
				command.description(),
				command.externalIdpGroupName());

		Group saved = groupRepository.save(group);
		publishEvent(IamEventTypes.GROUP_CREATED, command.tenantId(), saved.getId(), saved, context);
		return saved;
	}

	@Override
	@Transactional(readOnly = true)
	public Group getGroupById(TenantId tenantId, GroupId id) {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		Objects.requireNonNull(id, "GroupId must not be null.");

		return groupRepository.findById(id)
				.filter(g -> g.getTenantId().equals(tenantId))
				.orElseThrow(() -> new GroupNotFoundException(id));
	}

	@Override
	@Transactional
	public Group updateGroup(UpdateGroupCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Command must not be null.");
		Group group = getGroupById(command.tenantId(), command.groupId());

		group.updateDetails(command.name(), command.description(), command.externalIdpGroupName());
		Group saved = groupRepository.save(group);

		publishEvent(IamEventTypes.GROUP_UPDATED, command.tenantId(), saved.getId(), saved, context);
		return saved;
	}

	@Override
	@Transactional
	public void deleteGroup(DeleteGroupCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Command must not be null.");
		getGroupById(command.tenantId(), command.groupId()); // Ensure exists and belongs to tenant

		userGroupMembershipRepository.deleteAllByGroupId(command.groupId());
		groupRoleAssignmentRepository.deleteAllByGroupId(command.groupId());
		groupRepository.delete(command.groupId());

		publishEvent(IamEventTypes.GROUP_DELETED, command.tenantId(), command.groupId(), null, context);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Group> listGroups(TenantId tenantId) {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		return groupRepository.findAllByTenantId(tenantId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<User> getGroupMembers(TenantId tenantId, GroupId groupId) {
		getGroupById(tenantId, groupId); // Ensure exists
		List<UserGroupMembership> memberships = userGroupMembershipRepository.findAllByGroupId(groupId);
		List<UserId> userIds = memberships.stream().map(UserGroupMembership::userId).toList();
		return userRepository.findAllByIds(userIds);
	}

	@Override
	@Transactional
	public void addGroupMember(AddGroupMemberCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Command must not be null.");
		getGroupById(command.tenantId(), command.groupId());
		userRepository.findById(command.userId())
				.orElseThrow(() -> new UserNotFoundException(command.userId()));

		if (!userGroupMembershipRepository.existsByGroupIdAndUserId(command.groupId(), command.userId())) {
			userGroupMembershipRepository.save(UserGroupMembership.of(command.groupId(), command.userId()));
			publishEvent(IamEventTypes.GROUP_MEMBERSHIP_ADDED, command.tenantId(), command.groupId(), command.userId(), context);
		}
	}

	@Override
	@Transactional
	public void removeGroupMember(RemoveGroupMemberCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Command must not be null.");
		getGroupById(command.tenantId(), command.groupId());

		userGroupMembershipRepository.delete(command.groupId(), command.userId());
		publishEvent(IamEventTypes.GROUP_MEMBERSHIP_REMOVED, command.tenantId(), command.groupId(), command.userId(), context);
	}

	@Override
	@Transactional(readOnly = true)
	public List<GroupRoleAssignment> getGroupRoleAssignments(TenantId tenantId, GroupId groupId) {
		getGroupById(tenantId, groupId);
		return groupRoleAssignmentRepository.findAllByGroupId(groupId);
	}

	@Override
	@Transactional
	public GroupRoleAssignment assignRoleToGroup(AssignGroupRoleCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Command must not be null.");
		getGroupById(command.tenantId(), command.groupId());
		roleRepository.findById(command.roleId())
				.orElseThrow(() -> new RoleNotFoundException(command.roleId()));

		GroupRoleAssignment assignment;
		if (command.scopeNodeId() != null) {
			ScopeNode scopeNode = scopeNodeRepository.findById(command.scopeNodeId())
					.filter(s -> s.getTenantId().equals(command.tenantId()))
					.orElseThrow(() -> new ScopeNodeNotFoundException(command.scopeNodeId()));

			assignment = GroupRoleAssignment.forScope(
					command.groupId(), command.roleId(), command.tenantId(), scopeNode.getId(), command.inheritChildren());
		}
		else {
			assignment = GroupRoleAssignment.forTenant(command.groupId(), command.roleId(), command.tenantId());
		}

		GroupRoleAssignment saved = groupRoleAssignmentRepository.save(assignment);
		publishEvent(IamEventTypes.ROLE_ASSIGNMENT_CREATED, command.tenantId(), saved.getId(), saved, context);
		return saved;
	}

	@Override
	@Transactional
	public void revokeGroupRoleAssignment(GroupRoleAssignmentId assignmentId, OperationContext context) {
		Objects.requireNonNull(assignmentId, "GroupRoleAssignmentId must not be null.");
		GroupRoleAssignment assignment = groupRoleAssignmentRepository.findById(assignmentId)
				.orElseThrow(() -> new IllegalArgumentException("Group role assignment not found with ID: " + assignmentId.value()));

		groupRoleAssignmentRepository.delete(assignmentId);
		publishEvent(IamEventTypes.ROLE_ASSIGNMENT_REVOKED, assignment.tenantId(), assignmentId, null, context);
	}

	private void publishEvent(
			String eventType,
			TenantId tenantId,
			Object entityId,
			Object payload,
			OperationContext context) {
		IamEvent event = IamEvent.of(
				eventType,
				tenantId == null ? null : tenantId.value(),
				entityId instanceof io.github.edmaputra.uwati.iam.domain.model.GroupId gid ? gid.value() :
						entityId instanceof io.github.edmaputra.uwati.iam.domain.model.GroupRoleAssignmentId aid ? aid.value() : null,
				"GROUP",
				payload,
				context);
		eventPublisher.publishEvent(event);
	}
}
