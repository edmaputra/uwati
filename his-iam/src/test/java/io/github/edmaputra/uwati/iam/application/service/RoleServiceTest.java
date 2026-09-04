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
import io.github.edmaputra.uwati.iam.application.port.in.CreateRoleCommand;
import io.github.edmaputra.uwati.iam.application.port.in.DeleteRoleCommand;
import io.github.edmaputra.uwati.iam.application.port.in.UpdateRoleCommand;
import io.github.edmaputra.uwati.iam.domain.event.IamEvent;
import io.github.edmaputra.uwati.iam.domain.event.IamEventTypes;
import io.github.edmaputra.uwati.iam.domain.exception.RoleNotFoundException;
import io.github.edmaputra.uwati.iam.domain.model.Permissions;
import io.github.edmaputra.uwati.iam.domain.model.Role;
import io.github.edmaputra.uwati.iam.domain.model.RoleId;
import io.github.edmaputra.uwati.iam.domain.repository.GroupRoleAssignmentRepository;
import io.github.edmaputra.uwati.iam.domain.repository.RoleRepository;
import io.github.edmaputra.uwati.iam.domain.repository.UserRoleAssignmentRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RoleService}.
 *
 * @author edmaputra
 */
class RoleServiceTest {

	private RoleRepository roleRepository;
	private UserRoleAssignmentRepository userRoleAssignmentRepository;
	private GroupRoleAssignmentRepository groupRoleAssignmentRepository;
	private ApplicationEventPublisher eventPublisher;

	private RoleService roleService;
	private final TenantId tenantId = TenantId.generate();
	private final OperationContext context = OperationContext.of("admin@hospital.org", "trace-123");

	@BeforeEach
	void setUp() {
		roleRepository = Mockito.mock(RoleRepository.class);
		userRoleAssignmentRepository = Mockito.mock(UserRoleAssignmentRepository.class);
		groupRoleAssignmentRepository = Mockito.mock(GroupRoleAssignmentRepository.class);
		eventPublisher = Mockito.mock(ApplicationEventPublisher.class);

		roleService = new RoleService(
				roleRepository,
				userRoleAssignmentRepository,
				groupRoleAssignmentRepository,
				eventPublisher);
	}

	@Test
	@DisplayName("Should create custom role and publish event")
	void shouldCreateRoleSuccessfully() {
		when(roleRepository.existsByTenantIdAndCode(tenantId, "CUSTOM_ROLE")).thenReturn(false);
		when(roleRepository.save(any(Role.class))).thenAnswer(i -> i.getArgument(0));

		CreateRoleCommand command = new CreateRoleCommand(
				tenantId,
				"CUSTOM_ROLE",
				"Custom Role Name",
				"Description",
				Set.of(Permissions.PATIENT_READ, Permissions.PATIENT_WRITE));

		Role role = roleService.createRole(command, context);

		assertThat(role).isNotNull();
		assertThat(role.getCode()).isEqualTo("CUSTOM_ROLE");
		assertThat(role.getName()).isEqualTo("Custom Role Name");
		assertThat(role.permissions()).contains(Permissions.PATIENT_READ, Permissions.PATIENT_WRITE);
		assertThat(role.isSystemRole()).isFalse();

		ArgumentCaptor<IamEvent> eventCaptor = ArgumentCaptor.forClass(IamEvent.class);
		verify(eventPublisher).publishEvent(eventCaptor.capture());
		assertThat(eventCaptor.getValue().eventType()).isEqualTo(IamEventTypes.ROLE_CREATED);
	}

	@Test
	@DisplayName("Should prevent updating system role")
	void shouldPreventUpdatingSystemRole() {
		Role systemRole = Role.createSystemRole("ADMIN", "Administrator", null, Set.of("ALL"));
		when(roleRepository.findById(systemRole.getId())).thenReturn(Optional.of(systemRole));

		UpdateRoleCommand command = new UpdateRoleCommand(
				tenantId,
				systemRole.getId(),
				"New Name",
				null,
				Set.of("OTHER"));

		assertThatThrownBy(() -> roleService.updateRole(command, context))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("immutable and cannot be updated");
	}

	@Test
	@DisplayName("Should update custom role")
	void shouldUpdateCustomRole() {
		Role customRole = Role.createCustom(tenantId, "NURSE", "Nurse", null, Set.of("READ"));
		when(roleRepository.findById(customRole.getId())).thenReturn(Optional.of(customRole));
		when(roleRepository.save(any(Role.class))).thenAnswer(i -> i.getArgument(0));

		UpdateRoleCommand command = new UpdateRoleCommand(
				tenantId,
				customRole.getId(),
				"Senior Nurse",
				"Updated desc",
				Set.of("READ", "WRITE"));

		Role updated = roleService.updateRole(command, context);

		assertThat(updated.getName()).isEqualTo("Senior Nurse");
		assertThat(updated.permissions()).contains("READ", "WRITE");
	}

	@Test
	@DisplayName("Should prevent deleting system role")
	void shouldPreventDeletingSystemRole() {
		Role systemRole = Role.createSystemRole("ADMIN", "Administrator", null, Set.of("ALL"));
		when(roleRepository.findById(systemRole.getId())).thenReturn(Optional.of(systemRole));

		DeleteRoleCommand command = new DeleteRoleCommand(tenantId, systemRole.getId());

		assertThatThrownBy(() -> roleService.deleteRole(command, context))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("immutable and cannot be deleted");
	}

	@Test
	@DisplayName("Should prevent deleting role when assigned to users or groups")
	void shouldPreventDeletingAssignedRole() {
		Role customRole = Role.createCustom(tenantId, "NURSE", "Nurse", null, Set.of("READ"));
		when(roleRepository.findById(customRole.getId())).thenReturn(Optional.of(customRole));
		when(userRoleAssignmentRepository.existsByRoleId(customRole.getId())).thenReturn(true);

		DeleteRoleCommand command = new DeleteRoleCommand(tenantId, customRole.getId());

		assertThatThrownBy(() -> roleService.deleteRole(command, context))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("currently assigned to one or more users");
	}

	@Test
	@DisplayName("Should delete unassigned custom role")
	void shouldDeleteRoleSuccessfully() {
		Role customRole = Role.createCustom(tenantId, "NURSE", "Nurse", null, Set.of("READ"));
		when(roleRepository.findById(customRole.getId())).thenReturn(Optional.of(customRole));
		when(userRoleAssignmentRepository.existsByRoleId(customRole.getId())).thenReturn(false);
		when(groupRoleAssignmentRepository.existsByRoleId(customRole.getId())).thenReturn(false);

		roleService.deleteRole(new DeleteRoleCommand(tenantId, customRole.getId()), context);

		verify(roleRepository).delete(customRole.getId());
	}

	@Test
	@DisplayName("Should list available permissions catalog")
	void shouldListPermissions() {
		List<String> permissions = roleService.listAvailablePermissions();
		assertThat(permissions).isNotEmpty();
		assertThat(permissions).contains(Permissions.IAM_USER_READ, Permissions.PATIENT_READ);
	}
}
