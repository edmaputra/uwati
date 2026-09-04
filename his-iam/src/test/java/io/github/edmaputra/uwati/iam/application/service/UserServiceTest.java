package io.github.edmaputra.uwati.iam.application.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.application.model.EffectiveAccess;
import io.github.edmaputra.uwati.iam.application.port.in.AssignUserRoleCommand;
import io.github.edmaputra.uwati.iam.application.port.in.ChangeUserStatusCommand;
import io.github.edmaputra.uwati.iam.application.port.in.CreateUserCommand;
import io.github.edmaputra.uwati.iam.application.port.in.DeleteUserCommand;
import io.github.edmaputra.uwati.iam.application.port.in.UpdatePasswordCommand;
import io.github.edmaputra.uwati.iam.application.port.in.UpdateUserProfileCommand;
import io.github.edmaputra.uwati.iam.application.port.in.UserQuery;
import io.github.edmaputra.uwati.iam.application.port.out.PasswordEncoderPort;
import io.github.edmaputra.uwati.iam.domain.event.IamEvent;
import io.github.edmaputra.uwati.iam.domain.event.IamEventTypes;
import io.github.edmaputra.uwati.iam.domain.exception.RoleNotFoundException;
import io.github.edmaputra.uwati.iam.domain.exception.UserNotFoundException;
import io.github.edmaputra.uwati.iam.domain.model.Group;
import io.github.edmaputra.uwati.iam.domain.model.GroupId;
import io.github.edmaputra.uwati.iam.domain.model.Role;
import io.github.edmaputra.uwati.iam.domain.model.RoleId;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNode;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;
import io.github.edmaputra.uwati.iam.domain.model.User;
import io.github.edmaputra.uwati.iam.domain.model.UserGroupMembership;
import io.github.edmaputra.uwati.iam.domain.model.UserId;
import io.github.edmaputra.uwati.iam.domain.model.UserIdentity;
import io.github.edmaputra.uwati.iam.domain.model.UserIdentityId;
import io.github.edmaputra.uwati.iam.domain.model.UserRoleAssignment;
import io.github.edmaputra.uwati.iam.domain.model.UserRoleAssignmentId;
import io.github.edmaputra.uwati.iam.domain.model.UserStatus;
import io.github.edmaputra.uwati.iam.domain.repository.GroupRepository;
import io.github.edmaputra.uwati.iam.domain.repository.RoleRepository;
import io.github.edmaputra.uwati.iam.domain.repository.ScopeNodeRepository;
import io.github.edmaputra.uwati.iam.domain.repository.UserGroupMembershipRepository;
import io.github.edmaputra.uwati.iam.domain.repository.UserIdentityRepository;
import io.github.edmaputra.uwati.iam.domain.repository.UserRepository;
import io.github.edmaputra.uwati.iam.domain.repository.UserRoleAssignmentRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserService}.
 *
 * @author edmaputra
 */
class UserServiceTest {

	private UserRepository userRepository;
	private UserRoleAssignmentRepository userRoleAssignmentRepository;
	private UserGroupMembershipRepository userGroupMembershipRepository;
	private UserIdentityRepository userIdentityRepository;
	private RoleRepository roleRepository;
	private ScopeNodeRepository scopeNodeRepository;
	private GroupRepository groupRepository;
	private EffectiveAccessResolver effectiveAccessResolver;
	private PasswordEncoderPort passwordEncoder;
	private ApplicationEventPublisher eventPublisher;

	private UserService userService;
	private final OperationContext context = OperationContext.of("admin@hospital.org", "trace-123");

	@BeforeEach
	void setUp() {
		userRepository = Mockito.mock(UserRepository.class);
		userRoleAssignmentRepository = Mockito.mock(UserRoleAssignmentRepository.class);
		userGroupMembershipRepository = Mockito.mock(UserGroupMembershipRepository.class);
		userIdentityRepository = Mockito.mock(UserIdentityRepository.class);
		roleRepository = Mockito.mock(RoleRepository.class);
		scopeNodeRepository = Mockito.mock(ScopeNodeRepository.class);
		groupRepository = Mockito.mock(GroupRepository.class);
		effectiveAccessResolver = Mockito.mock(EffectiveAccessResolver.class);
		passwordEncoder = Mockito.mock(PasswordEncoderPort.class);
		eventPublisher = Mockito.mock(ApplicationEventPublisher.class);

		userService = new UserService(
				userRepository,
				userRoleAssignmentRepository,
				userGroupMembershipRepository,
				userIdentityRepository,
				roleRepository,
				scopeNodeRepository,
				groupRepository,
				effectiveAccessResolver,
				passwordEncoder,
				eventPublisher);
	}

	@Test
	@DisplayName("Should create user and encode password and publish event")
	void shouldCreateUserSuccessfully() {
		when(userRepository.existsByEmail("nurse@hospital.org")).thenReturn(false);
		when(passwordEncoder.encode("SecretPassword123")).thenReturn("hashed_pass");
		when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

		CreateUserCommand command = new CreateUserCommand(
				"nurse@hospital.org",
				"SecretPassword123",
				"Nurse Jackie",
				false,
				null,
				null,
				null,
				true,
				null);

		User user = userService.createUser(command, context);

		assertThat(user).isNotNull();
		assertThat(user.getEmail()).isEqualTo("nurse@hospital.org");
		assertThat(user.getFullName()).isEqualTo("Nurse Jackie");
		assertThat(user.getPasswordHash()).contains("hashed_pass");
		assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);

		ArgumentCaptor<IamEvent> eventCaptor = ArgumentCaptor.forClass(IamEvent.class);
		verify(eventPublisher).publishEvent(eventCaptor.capture());
		assertThat(eventCaptor.getValue().eventType()).isEqualTo(IamEventTypes.USER_CREATED);
	}

	@Test
	@DisplayName("Should create user with initial role and group")
	void shouldCreateUserWithRoleAndGroup() {
		TenantId tenantId = TenantId.generate();
		RoleId roleId = RoleId.generate();
		GroupId groupId = GroupId.generate();

		when(userRepository.existsByEmail("dr@hospital.org")).thenReturn(false);
		when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
		when(roleRepository.findById(roleId)).thenReturn(Optional.of(Role.createCustom(tenantId, "NURSE", "Nurse", null, Set.of("READ"))));
		when(groupRepository.findById(groupId)).thenReturn(Optional.of(Group.create(tenantId, "NURSES", "Nurses Group", null, null)));
		when(userRoleAssignmentRepository.save(any(UserRoleAssignment.class))).thenAnswer(i -> i.getArgument(0));
		when(userGroupMembershipRepository.save(any(UserGroupMembership.class))).thenAnswer(i -> i.getArgument(0));

		CreateUserCommand command = new CreateUserCommand(
				"dr@hospital.org",
				null,
				"Dr. Smith",
				false,
				tenantId,
				roleId,
				null,
				true,
				groupId);

		User user = userService.createUser(command, context);

		assertThat(user).isNotNull();
		verify(userRoleAssignmentRepository).save(any(UserRoleAssignment.class));
		verify(userGroupMembershipRepository).save(any(UserGroupMembership.class));
	}

	@Test
	@DisplayName("Should fail creating user with duplicate email")
	void shouldFailWhenEmailExists() {
		when(userRepository.existsByEmail("exist@hospital.org")).thenReturn(true);

		CreateUserCommand command = new CreateUserCommand(
				"exist@hospital.org",
				"pass",
				"Exist User",
				false,
				null,
				null,
				null,
				true,
				null);

		assertThatThrownBy(() -> userService.createUser(command, context))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("already exists");
	}

	@Test
	@DisplayName("Should update user profile")
	void shouldUpdateUserProfile() {
		User user = User.create("test@hospital.org", "hash", "Old Name", false);
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

		User updated = userService.updateUserProfile(new UpdateUserProfileCommand(user.getId(), "New Name"), context);

		assertThat(updated.getFullName()).isEqualTo("New Name");
		verify(eventPublisher).publishEvent(any(IamEvent.class));
	}

	@Test
	@DisplayName("Should change user status through state transitions")
	void shouldChangeUserStatus() {
		User user = User.create("test@hospital.org", "hash", "User Name", false);
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

		User suspended = userService.changeUserStatus(new ChangeUserStatusCommand(user.getId(), UserStatus.SUSPENDED), context);
		assertThat(suspended.getStatus()).isEqualTo(UserStatus.SUSPENDED);

		User activated = userService.changeUserStatus(new ChangeUserStatusCommand(user.getId(), UserStatus.ACTIVE), context);
		assertThat(activated.getStatus()).isEqualTo(UserStatus.ACTIVE);

		User deactivated = userService.changeUserStatus(new ChangeUserStatusCommand(user.getId(), UserStatus.DEACTIVATED), context);
		assertThat(deactivated.getStatus()).isEqualTo(UserStatus.DEACTIVATED);
	}

	@Test
	@DisplayName("Should update password")
	void shouldUpdatePassword() {
		User user = User.create("test@hospital.org", "oldHash", "User Name", false);
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(passwordEncoder.encode("NewSecret123")).thenReturn("newHash");

		userService.updatePassword(new UpdatePasswordCommand(user.getId(), "NewSecret123"), context);

		assertThat(user.getPasswordHash()).contains("newHash");
		verify(userRepository).save(user);
	}

	@Test
	@DisplayName("Should delete user and cleanup assignments, memberships, and identities")
	void shouldDeleteUserAndCleanup() {
		User user = User.create("test@hospital.org", "hash", "User Name", false);
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

		userService.deleteUser(new DeleteUserCommand(user.getId()), context);

		verify(userRoleAssignmentRepository).deleteAllByUserId(user.getId());
		verify(userGroupMembershipRepository).deleteAllByUserId(user.getId());
		verify(userIdentityRepository).deleteAllByUserId(user.getId());
		verify(userRepository).delete(user.getId());
	}

	@Test
	@DisplayName("Should resolve effective access for user in tenant")
	void shouldResolveEffectiveAccess() {
		User user = User.create("test@hospital.org", "hash", "User Name", false);
		TenantId tenantId = TenantId.generate();
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

		EffectiveAccess mockAccess = new EffectiveAccess(
				user.getId(),
				user.getEmail(),
				tenantId,
				false,
				true,
				Set.of("DOCTORS"),
				Set.of("PHYSICIAN"),
				Set.of("PATIENT_READ"),
				Set.of(),
				Set.of());

		when(effectiveAccessResolver.resolve(user, tenantId)).thenReturn(mockAccess);

		EffectiveAccess access = userService.getUserEffectiveAccess(user.getId(), tenantId);

		assertThat(access).isNotNull();
		assertThat(access.roles()).contains("PHYSICIAN");
		assertThat(access.permissions()).contains("PATIENT_READ");
	}

	@Test
	@DisplayName("Should assign role to user and revoke assignment")
	void shouldAssignAndRevokeRole() {
		User user = User.create("test@hospital.org", "hash", "User Name", false);
		TenantId tenantId = TenantId.generate();
		RoleId roleId = RoleId.generate();
		ScopeNodeId scopeId = ScopeNodeId.generate();

		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(roleRepository.findById(roleId)).thenReturn(Optional.of(Role.createCustom(tenantId, "NURSE", "Nurse", null, Set.of("READ"))));
		when(scopeNodeRepository.findById(scopeId)).thenReturn(Optional.of(ScopeNode.createRoot(tenantId, "ROOT", "Root Node")));
		when(userRoleAssignmentRepository.save(any(UserRoleAssignment.class))).thenAnswer(i -> i.getArgument(0));

		AssignUserRoleCommand assignCmd = new AssignUserRoleCommand(user.getId(), roleId, tenantId, scopeId, true);
		UserRoleAssignment assignment = userService.assignRoleToUser(assignCmd, context);

		assertThat(assignment).isNotNull();
		assertThat(assignment.getUserId()).isEqualTo(user.getId());

		UserRoleAssignmentId assignmentId = assignment.getId();
		when(userRoleAssignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));

		userService.revokeUserRoleAssignment(assignmentId, context);
		verify(userRoleAssignmentRepository).delete(assignmentId);
	}

	@Test
	@DisplayName("Should list and filter users by search keyword and status")
	void shouldListAndFilterUsers() {
		User user1 = User.create("alice@hospital.org", "hash", "Alice Smith", false);
		User user2 = User.create("bob@hospital.org", "hash", "Bob Jones", false);
		user2.suspend();

		when(userRepository.findAll()).thenReturn(List.of(user1, user2));

		List<User> all = userService.listUsers(null, null);
		assertThat(all).hasSize(2);

		List<User> searchResult = userService.listUsers(null, new UserQuery("alice", null));
		assertThat(searchResult).hasSize(1);
		assertThat(searchResult.getFirst().getEmail()).isEqualTo("alice@hospital.org");

		List<User> statusResult = userService.listUsers(null, new UserQuery(null, UserStatus.SUSPENDED));
		assertThat(statusResult).hasSize(1);
		assertThat(statusResult.getFirst().getEmail()).isEqualTo("bob@hospital.org");
	}
}
