package io.github.edmaputra.uwati.iam;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import io.github.edmaputra.uwati.iam.adapter.rest.GroupController;
import io.github.edmaputra.uwati.iam.adapter.rest.RoleController;
import io.github.edmaputra.uwati.iam.adapter.rest.UserController;
import io.github.edmaputra.uwati.iam.application.port.in.ManageGroupUseCase;
import io.github.edmaputra.uwati.iam.application.port.in.ManageRoleUseCase;
import io.github.edmaputra.uwati.iam.application.port.in.ManageUserUseCase;
import io.github.edmaputra.uwati.iam.application.port.out.PasswordEncoderPort;
import io.github.edmaputra.uwati.iam.application.service.EffectiveAccessResolver;
import io.github.edmaputra.uwati.iam.application.service.GroupService;
import io.github.edmaputra.uwati.iam.application.service.RoleService;
import io.github.edmaputra.uwati.iam.application.service.UserService;
import io.github.edmaputra.uwati.iam.domain.repository.GroupRepository;
import io.github.edmaputra.uwati.iam.domain.repository.GroupRoleAssignmentRepository;
import io.github.edmaputra.uwati.iam.domain.repository.RoleRepository;
import io.github.edmaputra.uwati.iam.domain.repository.ScopeNodeRepository;
import io.github.edmaputra.uwati.iam.domain.repository.UserGroupMembershipRepository;
import io.github.edmaputra.uwati.iam.domain.repository.UserIdentityRepository;
import io.github.edmaputra.uwati.iam.domain.repository.UserRepository;
import io.github.edmaputra.uwati.iam.domain.repository.UserRoleAssignmentRepository;

/**
 * Spring Boot auto-configuration for IAM management use cases and REST controllers
 * (Users, Groups, Roles, and Permissions).
 *
 * @author edmaputra
 */
@AutoConfiguration(after = {IamJpaAutoConfiguration.class, IamSecurityAutoConfiguration.class, IamScopeAutoConfiguration.class})
@Import({UserController.class, GroupController.class, RoleController.class})
public class IamManagementAutoConfiguration {

	/**
	 * Registers the {@link ManageUserUseCase} bean.
	 *
	 * @param userRepository               the user repository
	 * @param userRoleAssignmentRepository the user role assignment repository
	 * @param userGroupMembershipRepository the user group membership repository
	 * @param userIdentityRepository       the user identity repository
	 * @param roleRepository               the role repository
	 * @param scopeNodeRepository          the scope node repository
	 * @param groupRepository              the group repository
	 * @param effectiveAccessResolver      the effective access resolver
	 * @param passwordEncoder              the password encoder
	 * @param eventPublisher               the event publisher
	 * @return user management service
	 */
	@Bean
	@ConditionalOnMissingBean
	public ManageUserUseCase manageUserUseCase(
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
		return new UserService(
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

	/**
	 * Registers the {@link ManageGroupUseCase} bean.
	 *
	 * @param groupRepository               the group repository
	 * @param userGroupMembershipRepository the user group membership repository
	 * @param groupRoleAssignmentRepository the group role assignment repository
	 * @param roleRepository               the role repository
	 * @param scopeNodeRepository          the scope node repository
	 * @param userRepository               the user repository
	 * @param eventPublisher               the event publisher
	 * @return group management service
	 */
	@Bean
	@ConditionalOnMissingBean
	public ManageGroupUseCase manageGroupUseCase(
			GroupRepository groupRepository,
			UserGroupMembershipRepository userGroupMembershipRepository,
			GroupRoleAssignmentRepository groupRoleAssignmentRepository,
			RoleRepository roleRepository,
			ScopeNodeRepository scopeNodeRepository,
			UserRepository userRepository,
			ApplicationEventPublisher eventPublisher) {
		return new GroupService(
				groupRepository,
				userGroupMembershipRepository,
				groupRoleAssignmentRepository,
				roleRepository,
				scopeNodeRepository,
				userRepository,
				eventPublisher);
	}

	/**
	 * Registers the {@link ManageRoleUseCase} bean.
	 *
	 * @param roleRepository               the role repository
	 * @param userRoleAssignmentRepository the user role assignment repository
	 * @param groupRoleAssignmentRepository the group role assignment repository
	 * @param eventPublisher               the event publisher
	 * @return role management service
	 */
	@Bean
	@ConditionalOnMissingBean
	public ManageRoleUseCase manageRoleUseCase(
			RoleRepository roleRepository,
			UserRoleAssignmentRepository userRoleAssignmentRepository,
			GroupRoleAssignmentRepository groupRoleAssignmentRepository,
			ApplicationEventPublisher eventPublisher) {
		return new RoleService(
				roleRepository,
				userRoleAssignmentRepository,
				groupRoleAssignmentRepository,
				eventPublisher);
	}
}
