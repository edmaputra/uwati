package io.github.edmaputra.uwati.iam.application.service;

import java.util.List;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.application.port.in.CreateRoleCommand;
import io.github.edmaputra.uwati.iam.application.port.in.DeleteRoleCommand;
import io.github.edmaputra.uwati.iam.application.port.in.ManageRoleUseCase;
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

/**
 * Application service implementing {@link ManageRoleUseCase}.
 * Manages custom tenant roles, permission catalogs, and immutability safeguards.
 *
 * @author edmaputra
 */
public class RoleService implements ManageRoleUseCase {

	private final RoleRepository roleRepository;
	private final UserRoleAssignmentRepository userRoleAssignmentRepository;
	private final GroupRoleAssignmentRepository groupRoleAssignmentRepository;
	private final ApplicationEventPublisher eventPublisher;

	/**
	 * Constructs the role service with all required dependencies.
	 *
	 * @param roleRepository               the role repository
	 * @param userRoleAssignmentRepository the user role assignment repository
	 * @param groupRoleAssignmentRepository the group role assignment repository
	 * @param eventPublisher               the application event publisher
	 */
	public RoleService(
			RoleRepository roleRepository,
			UserRoleAssignmentRepository userRoleAssignmentRepository,
			GroupRoleAssignmentRepository groupRoleAssignmentRepository,
			ApplicationEventPublisher eventPublisher) {
		this.roleRepository = Objects.requireNonNull(roleRepository, "RoleRepository must not be null.");
		this.userRoleAssignmentRepository = Objects.requireNonNull(userRoleAssignmentRepository, "UserRoleAssignmentRepository must not be null.");
		this.groupRoleAssignmentRepository = Objects.requireNonNull(groupRoleAssignmentRepository, "GroupRoleAssignmentRepository must not be null.");
		this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher must not be null.");
	}

	@Override
	@Transactional
	public Role createRole(CreateRoleCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Command must not be null.");

		if (command.tenantId() != null && roleRepository.existsByTenantIdAndCode(command.tenantId(), command.code())) {
			throw new IllegalArgumentException("A role with code '" + command.code() + "' already exists for this tenant.");
		}

		Role role = Role.createCustom(
				command.tenantId(),
				command.code(),
				command.name(),
				command.description(),
				command.permissions());

		Role saved = roleRepository.save(role);
		publishEvent(IamEventTypes.ROLE_CREATED, command.tenantId(), saved.getId(), saved, context);
		return saved;
	}

	@Override
	@Transactional(readOnly = true)
	public Role getRoleById(RoleId id) {
		Objects.requireNonNull(id, "RoleId must not be null.");
		return roleRepository.findById(id).orElseThrow(() -> new RoleNotFoundException(id));
	}

	@Override
	@Transactional
	public Role updateRole(UpdateRoleCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Command must not be null.");
		Role role = getRoleById(command.roleId());

		if (role.isSystemRole()) {
			throw new IllegalStateException("System role '" + role.getCode() + "' is immutable and cannot be updated.");
		}

		role.update(command.name(), command.description(), command.permissions());
		Role saved = roleRepository.save(role);

		publishEvent(IamEventTypes.ROLE_UPDATED, role.optionalTenantId().orElse(null), saved.getId(), saved, context);
		return saved;
	}

	@Override
	@Transactional
	public void deleteRole(DeleteRoleCommand command, OperationContext context) {
		Objects.requireNonNull(command, "Command must not be null.");
		Role role = getRoleById(command.roleId());

		if (role.isSystemRole()) {
			throw new IllegalStateException("System role '" + role.getCode() + "' is immutable and cannot be deleted.");
		}

		if (userRoleAssignmentRepository.existsByRoleId(command.roleId())) {
			throw new IllegalStateException("Cannot delete role '" + role.getCode() + "' because it is currently assigned to one or more users.");
		}

		if (groupRoleAssignmentRepository.existsByRoleId(command.roleId())) {
			throw new IllegalStateException("Cannot delete role '" + role.getCode() + "' because it is currently assigned to one or more groups.");
		}

		roleRepository.delete(command.roleId());
		publishEvent(IamEventTypes.ROLE_DELETED, role.optionalTenantId().orElse(null), command.roleId(), null, context);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Role> listRoles(TenantId tenantId, String typeFilter) {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		List<Role> roles = roleRepository.findAllByTenantIdOrGlobal(tenantId);

		if (typeFilter == null || typeFilter.isBlank() || typeFilter.equalsIgnoreCase("ALL")) {
			return roles;
		}
		if (typeFilter.equalsIgnoreCase("SYSTEM")) {
			return roles.stream().filter(Role::isSystemRole).toList();
		}
		if (typeFilter.equalsIgnoreCase("CUSTOM")) {
			return roles.stream().filter(r -> !r.isSystemRole()).toList();
		}
		return roles;
	}

	@Override
	public List<String> listAvailablePermissions() {
		return Permissions.all().stream().sorted().toList();
	}

	private void publishEvent(
			String eventType,
			TenantId tenantId,
			RoleId roleId,
			Object payload,
			OperationContext context) {
		IamEvent event = IamEvent.of(
				eventType,
				tenantId == null ? null : tenantId.value(),
				roleId == null ? null : roleId.value(),
				"ROLE",
				payload,
				context);
		eventPublisher.publishEvent(event);
	}
}
