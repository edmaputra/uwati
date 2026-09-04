package io.github.edmaputra.uwati.iam;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.github.edmaputra.uwati.iam.adapter.persistence.adapter.GroupRepositoryAdapter;
import io.github.edmaputra.uwati.iam.adapter.persistence.adapter.GroupRoleAssignmentRepositoryAdapter;
import io.github.edmaputra.uwati.iam.adapter.persistence.adapter.RoleRepositoryAdapter;
import io.github.edmaputra.uwati.iam.adapter.persistence.adapter.ScopeNodeRepositoryAdapter;
import io.github.edmaputra.uwati.iam.adapter.persistence.adapter.UserGroupMembershipRepositoryAdapter;
import io.github.edmaputra.uwati.iam.adapter.persistence.adapter.UserIdentityRepositoryAdapter;
import io.github.edmaputra.uwati.iam.adapter.persistence.adapter.UserRepositoryAdapter;
import io.github.edmaputra.uwati.iam.adapter.persistence.adapter.UserRoleAssignmentRepositoryAdapter;
import io.github.edmaputra.uwati.iam.adapter.persistence.repository.GroupJpaRepository;
import io.github.edmaputra.uwati.iam.adapter.persistence.repository.GroupRoleAssignmentJpaRepository;
import io.github.edmaputra.uwati.iam.adapter.persistence.repository.RoleJpaRepository;
import io.github.edmaputra.uwati.iam.adapter.persistence.repository.ScopeNodeJpaRepository;
import io.github.edmaputra.uwati.iam.adapter.persistence.repository.UserGroupMembershipJpaRepository;
import io.github.edmaputra.uwati.iam.adapter.persistence.repository.UserIdentityJpaRepository;
import io.github.edmaputra.uwati.iam.adapter.persistence.repository.UserJpaRepository;
import io.github.edmaputra.uwati.iam.adapter.persistence.repository.UserRoleAssignmentJpaRepository;
import io.github.edmaputra.uwati.iam.domain.repository.GroupRepository;
import io.github.edmaputra.uwati.iam.domain.repository.GroupRoleAssignmentRepository;
import io.github.edmaputra.uwati.iam.domain.repository.RoleRepository;
import io.github.edmaputra.uwati.iam.domain.repository.ScopeNodeRepository;
import io.github.edmaputra.uwati.iam.domain.repository.UserGroupMembershipRepository;
import io.github.edmaputra.uwati.iam.domain.repository.UserIdentityRepository;
import io.github.edmaputra.uwati.iam.domain.repository.UserRepository;
import io.github.edmaputra.uwati.iam.domain.repository.UserRoleAssignmentRepository;

/**
 * Spring Boot auto-configuration for IAM JPA entities, Spring Data repositories,
 * and domain persistence adapters.
 *
 * @author edmaputra
 */
@AutoConfiguration
@EntityScan(basePackages = "io.github.edmaputra.uwati.iam.adapter.persistence.entity")
@EnableJpaRepositories(basePackages = "io.github.edmaputra.uwati.iam.adapter.persistence.repository")
public class IamJpaAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public ScopeNodeRepository scopeNodeRepository(ScopeNodeJpaRepository repository) {
		return new ScopeNodeRepositoryAdapter(repository);
	}

	@Bean
	@ConditionalOnMissingBean
	public UserRepository userRepository(UserJpaRepository repository) {
		return new UserRepositoryAdapter(repository);
	}

	@Bean
	@ConditionalOnMissingBean
	public RoleRepository roleRepository(RoleJpaRepository repository) {
		return new RoleRepositoryAdapter(repository);
	}

	@Bean
	@ConditionalOnMissingBean
	public GroupRepository groupRepository(GroupJpaRepository repository) {
		return new GroupRepositoryAdapter(repository);
	}

	@Bean
	@ConditionalOnMissingBean
	public UserGroupMembershipRepository userGroupMembershipRepository(UserGroupMembershipJpaRepository repository) {
		return new UserGroupMembershipRepositoryAdapter(repository);
	}

	@Bean
	@ConditionalOnMissingBean
	public UserRoleAssignmentRepository userRoleAssignmentRepository(UserRoleAssignmentJpaRepository repository) {
		return new UserRoleAssignmentRepositoryAdapter(repository);
	}

	@Bean
	@ConditionalOnMissingBean
	public GroupRoleAssignmentRepository groupRoleAssignmentRepository(GroupRoleAssignmentJpaRepository repository) {
		return new GroupRoleAssignmentRepositoryAdapter(repository);
	}

	@Bean
	@ConditionalOnMissingBean
	public UserIdentityRepository userIdentityRepository(UserIdentityJpaRepository repository) {
		return new UserIdentityRepositoryAdapter(repository);
	}
}
