package io.github.edmaputra.uwati.iam.application.service;

import java.util.List;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.application.model.EffectiveAccess;
import io.github.edmaputra.uwati.iam.application.port.in.AssignUserRoleCommand;
import io.github.edmaputra.uwati.iam.application.port.in.ChangeUserStatusCommand;
import io.github.edmaputra.uwati.iam.application.port.in.CreateUserCommand;
import io.github.edmaputra.uwati.iam.application.port.in.DeleteUserCommand;
import io.github.edmaputra.uwati.iam.application.port.in.ManageUserUseCase;
import io.github.edmaputra.uwati.iam.application.port.in.UpdatePasswordCommand;
import io.github.edmaputra.uwati.iam.application.port.in.UpdateUserProfileCommand;
import io.github.edmaputra.uwati.iam.application.port.in.UserQuery;
import io.github.edmaputra.uwati.iam.application.port.out.PasswordEncoderPort;
import io.github.edmaputra.uwati.iam.domain.event.IamEvent;
import io.github.edmaputra.uwati.iam.domain.event.IamEventTypes;
import io.github.edmaputra.uwati.iam.domain.exception.RoleNotFoundException;
import io.github.edmaputra.uwati.iam.domain.exception.ScopeNodeNotFoundException;
import io.github.edmaputra.uwati.iam.domain.exception.UserNotFoundException;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNode;
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

/**
 * Application service implementing {@link ManageUserUseCase}.
 * Coordinates user provisioning, state machine transitions, password management,
 * identity linking, and direct role assignment lifecycle.
 *
 * @author edmaputra
 */
public class UserService implements ManageUserUseCase {

	private final UserRepository userRepository;
	private final UserRoleAssignmentRepository userRoleAssignmentRepository;
	private final UserGroupMembershipRepository userGroupMembershipRepository;
	private final UserIdentityRepository userIdentityRepository;
	private final RoleRepository roleRepository;
	private final ScopeNodeRepository scopeNodeRepository;
	private final GroupRepository groupRepository;
	private final EffectiveAccessResolver effectiveAccessResolver;
	private final PasswordEncoderPort passwordEncoder;
	private final ApplicationEventPublisher eventPublisher;

	/**
	 * Constructs the user service with all required dependencies.
	 *
	 * @param userRepository               the user repository
	 * @param userRoleAssignmentRepository the user role assignment repository
	 * @param userGroupMembershipRepository the user group membership repository
	 * @param userIdentityRepository       the user identity repository
	 * @param roleRepository               the role repository
	 * @param scopeNodeRepository          the scope node repository
	 * @param groupRepository              the group repository
	 * @param effectiveAccessResolver      the effective access resolver
	 * @param passwordEncoder              the password encoder port
	 * @param eventPublisher               the application event publisher
	 */
	public UserService(
			UserRepository userRepository,
			UserRoleAssignmentRepository userRoleAssignmentRepository,
			UserGroupMembershipRepository userGroupMembershipRepository,
			UserIdentityRepository userIdentityRepository,
			RoleRepository roleRepository,
			ScopeNodeRepository scopeNodeRepository,
			GroupRepository groupRepository,
			EffectiveAccessResolver effectiveAccessResolver,
			PasswordEncoderPort passwordEncoder,
			ApplicationEventPublisher eventPublisher) {
		this.userRepository = Objects.requireNonNull(userRepository, "UserRepository must not be null.");
		this.userRoleAssignmentRepository = Objects.requireNonNull(userRoleAssignmentRepository, "UserRoleAssignmentRepository must not be null.");
		this.userGroupMembershipRepository = Objects.requireNonNull(userGroupMembershipRepository, "UserGroupMembershipRepository must not be null.");
		this.userIdentityRepository = Objects.requireNonNull(userIdentityRepository, "UserIdentityRepository must not be null.");
		this.roleRepository = Objects.requireNonNull(roleRepository, "RoleRepository must not be null.");
		this.scopeNodeRepository = Objects.requireNonNull(scopeNodeRepository, "ScopeNodeRepository must not be null.");
		this.groupRepository = Objects.requireNonNull(groupRepository, "GroupRepository must not be null.");
		this.effectiveAccessResolver = Objects.requireNonNull(effectiveAccessResolver, "EffectiveAccessResolver must not be null.");
		this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "PasswordEncoderPort must not be null.");
		this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher must not be null.");
	}

	@Override
	@Transactional
	public User createUser(CreateUserCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Command must not be null.");

		if (userRepository.existsByEmail(command.email())) {
			throw new IllegalArgumentException("A user with email '" + command.email() + "' already exists.");
		}

		String passwordHash = null;
		if (command.rawPassword() != null && !command.rawPassword().isBlank()) {
			passwordHash = passwordEncoder.encode(command.rawPassword());
		}

		User user = User.create(command.email(), passwordHash, command.fullName(), command.platformSuperAdmin());
		User saved = userRepository.save(user);

		// Handle optional initial role assignment
		if (command.roleId() != null) {
			roleRepository.findById(command.roleId())
					.orElseThrow(() -> new RoleNotFoundException(command.roleId()));

			if (command.scopeNodeId() != null) {
				if (command.tenantId() == null) {
					throw new IllegalArgumentException("TenantId is required when assigning role to a scope node.");
				}
				ScopeNode scopeNode = scopeNodeRepository.findById(command.scopeNodeId())
						.filter(s -> s.getTenantId().equals(command.tenantId()))
						.orElseThrow(() -> new ScopeNodeNotFoundException(command.scopeNodeId()));

				userRoleAssignmentRepository.save(UserRoleAssignment.forScope(
						saved.getId(), command.roleId(), command.tenantId(), scopeNode.getId(), command.inheritChildren()));
			}
			else if (command.tenantId() != null) {
				userRoleAssignmentRepository.save(UserRoleAssignment.forTenant(saved.getId(), command.roleId(), command.tenantId()));
			}
			else if (command.platformSuperAdmin()) {
				userRoleAssignmentRepository.save(UserRoleAssignment.createGlobalSuperadmin(saved.getId(), command.roleId()));
			}
		}

		// Handle optional initial group membership
		if (command.groupId() != null) {
			groupRepository.findById(command.groupId())
					.orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + command.groupId().value()));
			userGroupMembershipRepository.save(UserGroupMembership.of(command.groupId(), saved.getId()));
		}

		publishEvent(IamEventTypes.USER_CREATED, command.tenantId(), saved.getId(), saved, context);
		return saved;
	}

	@Override
	@Transactional(readOnly = true)
	public User getUserById(UserId id) {
		Objects.requireNonNull(id, "UserId must not be null.");
		return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
	}

	@Override
	@Transactional
	public User updateUserProfile(UpdateUserProfileCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Command must not be null.");
		User user = getUserById(command.userId());
		user.updateProfile(command.fullName());
		User saved = userRepository.save(user);

		publishEvent(IamEventTypes.USER_UPDATED, null, saved.getId(), saved, context);
		return saved;
	}

	@Override
	@Transactional
	public User changeUserStatus(ChangeUserStatusCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Command must not be null.");
		User user = getUserById(command.userId());

		if (command.status() == UserStatus.ACTIVE) {
			user.activate();
		}
		else if (command.status() == UserStatus.SUSPENDED) {
			user.suspend();
		}
		else if (command.status() == UserStatus.DEACTIVATED) {
			user.deactivate();
		}

		User saved = userRepository.save(user);
		publishEvent(IamEventTypes.USER_STATUS_CHANGED, null, saved.getId(), saved, context);

		if (saved.isDeactivated()) {
			publishEvent(IamEventTypes.USER_DEACTIVATED, null, saved.getId(), saved, context);
		}
		return saved;
	}

	@Override
	@Transactional
	public void updatePassword(UpdatePasswordCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Command must not be null.");
		User user = getUserById(command.userId());
		String newHash = passwordEncoder.encode(command.newPassword());
		user.updatePassword(newHash);
		userRepository.save(user);

		publishEvent(IamEventTypes.USER_PASSWORD_RESET, null, user.getId(), null, context);
	}

	@Override
	@Transactional
	public void deleteUser(DeleteUserCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Command must not be null.");
		User user = getUserById(command.userId());

		userRoleAssignmentRepository.deleteAllByUserId(command.userId());
		userGroupMembershipRepository.deleteAllByUserId(command.userId());
		userIdentityRepository.deleteAllByUserId(command.userId());
		userRepository.delete(command.userId());

		publishEvent(IamEventTypes.USER_DEACTIVATED, null, user.getId(), null, context);
	}

	@Override
	@Transactional(readOnly = true)
	public EffectiveAccess getUserEffectiveAccess(UserId userId, TenantId tenantId) {
		Objects.requireNonNull(userId, "UserId must not be null.");
		User user = getUserById(userId);
		return effectiveAccessResolver.resolve(user, tenantId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserIdentity> getUserIdentities(UserId userId) {
		Objects.requireNonNull(userId, "UserId must not be null.");
		getUserById(userId);
		return userIdentityRepository.findAllByUserId(userId);
	}

	@Override
	@Transactional
	public void unlinkUserIdentity(UserId userId, UserIdentityId identityId, OperationContext context) {
		Objects.requireNonNull(userId, "UserId must not be null.");
		Objects.requireNonNull(identityId, "UserIdentityId must not be null.");

		UserIdentity identity = userIdentityRepository.findById(identityId)
				.filter(i -> i.getUserId().equals(userId))
				.orElseThrow(() -> new IllegalArgumentException("User identity not found or does not belong to user: " + identityId.value()));

		userIdentityRepository.delete(identityId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserRoleAssignment> getUserRoleAssignments(UserId userId, TenantId tenantId) {
		Objects.requireNonNull(userId, "UserId must not be null.");
		if (tenantId != null) {
			return userRoleAssignmentRepository.findAllByUserIdAndTenantId(userId, tenantId);
		}
		return userRoleAssignmentRepository.findAllByUserId(userId);
	}

	@Override
	@Transactional
	public UserRoleAssignment assignRoleToUser(AssignUserRoleCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Command must not be null.");
		getUserById(command.userId());
		roleRepository.findById(command.roleId())
				.orElseThrow(() -> new RoleNotFoundException(command.roleId()));

		UserRoleAssignment assignment;
		if (command.scopeNodeId() != null) {
			if (command.tenantId() == null) {
				throw new IllegalArgumentException("TenantId is required when assigning role to a scope node.");
			}
			ScopeNode scopeNode = scopeNodeRepository.findById(command.scopeNodeId())
					.filter(s -> s.getTenantId().equals(command.tenantId()))
					.orElseThrow(() -> new ScopeNodeNotFoundException(command.scopeNodeId()));

			assignment = UserRoleAssignment.forScope(
					command.userId(), command.roleId(), command.tenantId(), scopeNode.getId(), command.inheritChildren());
		}
		else if (command.tenantId() != null) {
			assignment = UserRoleAssignment.forTenant(command.userId(), command.roleId(), command.tenantId());
		}
		else {
			assignment = UserRoleAssignment.createGlobalSuperadmin(command.userId(), command.roleId());
		}

		UserRoleAssignment saved = userRoleAssignmentRepository.save(assignment);
		publishEvent(IamEventTypes.ROLE_ASSIGNMENT_CREATED, command.tenantId(), saved.getId(), saved, context);
		return saved;
	}

	@Override
	@Transactional
	public void revokeUserRoleAssignment(UserRoleAssignmentId assignmentId, OperationContext context) {
		Objects.requireNonNull(assignmentId, "UserRoleAssignmentId must not be null.");
		UserRoleAssignment assignment = userRoleAssignmentRepository.findById(assignmentId)
				.orElseThrow(() -> new IllegalArgumentException("Role assignment not found with ID: " + assignmentId.value()));

		userRoleAssignmentRepository.delete(assignmentId);
		publishEvent(IamEventTypes.ROLE_ASSIGNMENT_REVOKED, assignment.optionalTenantId().orElse(null), assignmentId, null, context);
	}

	@Override
	@Transactional(readOnly = true)
	public List<User> listUsers(TenantId tenantId, UserQuery query) {
		List<User> users;
		if (tenantId != null) {
			users = userRepository.findAllByTenantId(tenantId);
		}
		else {
			users = userRepository.findAll();
		}

		if (query == null) {
			return users;
		}

		return users.stream()
				.filter(u -> {
					if (query.status() != null && u.getStatus() != query.status()) {
						return false;
					}
					if (query.search() != null && !query.search().isBlank()) {
						String term = query.search().trim().toLowerCase();
						return u.getEmail().toLowerCase().contains(term) || u.getFullName().toLowerCase().contains(term);
					}
					return true;
				})
				.toList();
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
				entityId instanceof io.github.edmaputra.uwati.iam.domain.model.UserId uid ? uid.value() :
						entityId instanceof io.github.edmaputra.uwati.iam.domain.model.UserRoleAssignmentId aid ? aid.value() : null,
				"USER",
				payload,
				context);
		eventPublisher.publishEvent(event);
	}
}
