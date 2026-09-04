package io.github.edmaputra.uwati.iam.application.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.application.port.in.AddGroupMemberCommand;
import io.github.edmaputra.uwati.iam.application.port.in.AssignGroupRoleCommand;
import io.github.edmaputra.uwati.iam.application.port.in.CreateGroupCommand;
import io.github.edmaputra.uwati.iam.application.port.in.DeleteGroupCommand;
import io.github.edmaputra.uwati.iam.application.port.in.RemoveGroupMemberCommand;
import io.github.edmaputra.uwati.iam.application.port.in.UpdateGroupCommand;
import io.github.edmaputra.uwati.iam.domain.event.IamEvent;
import io.github.edmaputra.uwati.iam.domain.event.IamEventTypes;
import io.github.edmaputra.uwati.iam.domain.exception.GroupNotFoundException;
import io.github.edmaputra.uwati.iam.domain.model.Group;
import io.github.edmaputra.uwati.iam.domain.model.GroupId;
import io.github.edmaputra.uwati.iam.domain.model.GroupRoleAssignment;
import io.github.edmaputra.uwati.iam.domain.model.GroupRoleAssignmentId;
import io.github.edmaputra.uwati.iam.domain.model.Role;
import io.github.edmaputra.uwati.iam.domain.model.RoleId;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNode;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;
import io.github.edmaputra.uwati.iam.domain.model.User;
import io.github.edmaputra.uwati.iam.domain.model.UserGroupMembership;
import io.github.edmaputra.uwati.iam.domain.model.UserId;
import io.github.edmaputra.uwati.iam.domain.repository.GroupRepository;
import io.github.edmaputra.uwati.iam.domain.repository.GroupRoleAssignmentRepository;
import io.github.edmaputra.uwati.iam.domain.repository.RoleRepository;
import io.github.edmaputra.uwati.iam.domain.repository.ScopeNodeRepository;
import io.github.edmaputra.uwati.iam.domain.repository.UserGroupMembershipRepository;
import io.github.edmaputra.uwati.iam.domain.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GroupService}.
 *
 * @author edmaputra
 */
class GroupServiceTest {

	private GroupRepository groupRepository;
	private UserGroupMembershipRepository userGroupMembershipRepository;
	private GroupRoleAssignmentRepository groupRoleAssignmentRepository;
	private RoleRepository roleRepository;
	private ScopeNodeRepository scopeNodeRepository;
	private UserRepository userRepository;
	private ApplicationEventPublisher eventPublisher;

	private GroupService groupService;
	private final TenantId tenantId = TenantId.generate();
	private final OperationContext context = OperationContext.of("admin@hospital.org", "trace-123");

	@BeforeEach
	void setUp() {
		groupRepository = Mockito.mock(GroupRepository.class);
		userGroupMembershipRepository = Mockito.mock(UserGroupMembershipRepository.class);
		groupRoleAssignmentRepository = Mockito.mock(GroupRoleAssignmentRepository.class);
		roleRepository = Mockito.mock(RoleRepository.class);
		scopeNodeRepository = Mockito.mock(ScopeNodeRepository.class);
		userRepository = Mockito.mock(UserRepository.class);
		eventPublisher = Mockito.mock(ApplicationEventPublisher.class);

		groupService = new GroupService(
				groupRepository,
				userGroupMembershipRepository,
				groupRoleAssignmentRepository,
				roleRepository,
				scopeNodeRepository,
				userRepository,
				eventPublisher);
	}

	@Test
	@DisplayName("Should create group and publish event")
	void shouldCreateGroupSuccessfully() {
		when(groupRepository.existsByTenantIdAndCode(tenantId, "DOCTORS")).thenReturn(false);
		when(groupRepository.save(any(Group.class))).thenAnswer(i -> i.getArgument(0));

		CreateGroupCommand command = new CreateGroupCommand(
				tenantId,
				"DOCTORS",
				"Medical Doctors",
				"Staff Physicians",
				"oidc-doctors");

		Group group = groupService.createGroup(command, context);

		assertThat(group).isNotNull();
		assertThat(group.getCode()).isEqualTo("DOCTORS");
		assertThat(group.getName()).isEqualTo("Medical Doctors");
		assertThat(group.optionalExternalIdpGroupName()).contains("oidc-doctors");

		ArgumentCaptor<IamEvent> eventCaptor = ArgumentCaptor.forClass(IamEvent.class);
		verify(eventPublisher).publishEvent(eventCaptor.capture());
		assertThat(eventCaptor.getValue().eventType()).isEqualTo(IamEventTypes.GROUP_CREATED);
	}

	@Test
	@DisplayName("Should fail when creating group with duplicate code in tenant")
	void shouldFailWhenDuplicateGroupCode() {
		when(groupRepository.existsByTenantIdAndCode(tenantId, "DOCTORS")).thenReturn(true);

		CreateGroupCommand command = new CreateGroupCommand(
				tenantId,
				"DOCTORS",
				"Medical Doctors",
				null,
				null);

		assertThatThrownBy(() -> groupService.createGroup(command, context))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("already exists");
	}

	@Test
	@DisplayName("Should update group details")
	void shouldUpdateGroup() {
		Group group = Group.create(tenantId, "DOCTORS", "Doctors", "Desc", "idp-group");
		when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
		when(groupRepository.save(any(Group.class))).thenAnswer(i -> i.getArgument(0));

		UpdateGroupCommand command = new UpdateGroupCommand(
				tenantId,
				group.getId(),
				"Updated Doctors",
				"New Desc",
				"new-idp-group");

		Group updated = groupService.updateGroup(command, context);

		assertThat(updated.getName()).isEqualTo("Updated Doctors");
		assertThat(updated.optionalDescription()).contains("New Desc");
		verify(eventPublisher).publishEvent(any(IamEvent.class));
	}

	@Test
	@DisplayName("Should delete group and clean up memberships and role bindings")
	void shouldDeleteGroupAndCleanup() {
		Group group = Group.create(tenantId, "DOCTORS", "Doctors", null, null);
		when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));

		groupService.deleteGroup(new DeleteGroupCommand(tenantId, group.getId()), context);

		verify(userGroupMembershipRepository).deleteAllByGroupId(group.getId());
		verify(groupRoleAssignmentRepository).deleteAllByGroupId(group.getId());
		verify(groupRepository).delete(group.getId());
	}

	@Test
	@DisplayName("Should add and remove group member")
	void shouldAddAndRemoveMember() {
		Group group = Group.create(tenantId, "DOCTORS", "Doctors", null, null);
		UserId userId = UserId.generate();
		User user = User.create("doc@hospital.org", "hash", "Dr. Test", false);

		when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userGroupMembershipRepository.existsByGroupIdAndUserId(group.getId(), userId)).thenReturn(false);

		groupService.addGroupMember(new AddGroupMemberCommand(tenantId, group.getId(), userId), context);
		verify(userGroupMembershipRepository).save(any(UserGroupMembership.class));

		groupService.removeGroupMember(new RemoveGroupMemberCommand(tenantId, group.getId(), userId), context);
		verify(userGroupMembershipRepository).delete(group.getId(), userId);
	}

	@Test
	@DisplayName("Should assign role to group and revoke it")
	void shouldAssignAndRevokeGroupRole() {
		Group group = Group.create(tenantId, "DOCTORS", "Doctors", null, null);
		RoleId roleId = RoleId.generate();
		ScopeNodeId scopeId = ScopeNodeId.generate();

		when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
		when(roleRepository.findById(roleId)).thenReturn(Optional.of(Role.createCustom(tenantId, "PHYSICIAN", "Physician", null, Set.of("READ"))));
		when(scopeNodeRepository.findById(scopeId)).thenReturn(Optional.of(ScopeNode.createRoot(tenantId, "ROOT", "Root Node")));
		when(groupRoleAssignmentRepository.save(any(GroupRoleAssignment.class))).thenAnswer(i -> i.getArgument(0));

		AssignGroupRoleCommand assignCmd = new AssignGroupRoleCommand(tenantId, group.getId(), roleId, scopeId, true);
		GroupRoleAssignment assignment = groupService.assignRoleToGroup(assignCmd, context);

		assertThat(assignment).isNotNull();
		assertThat(assignment.getGroupId()).isEqualTo(group.getId());

		GroupRoleAssignmentId assignmentId = assignment.getId();
		when(groupRoleAssignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));

		groupService.revokeGroupRoleAssignment(assignmentId, context);
		verify(groupRoleAssignmentRepository).delete(assignmentId);
	}
}
