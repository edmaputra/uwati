package io.github.edmaputra.uwati.iam.adapter.rest;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import io.github.edmaputra.uwati.domain.security.CurrentActorProvider;
import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.application.port.in.CreateRoleCommand;
import io.github.edmaputra.uwati.iam.application.port.in.DeleteRoleCommand;
import io.github.edmaputra.uwati.iam.application.port.in.ManageRoleUseCase;
import io.github.edmaputra.uwati.iam.application.port.in.UpdateRoleCommand;
import io.github.edmaputra.uwati.iam.domain.model.Permissions;
import io.github.edmaputra.uwati.iam.domain.model.Role;
import io.github.edmaputra.uwati.iam.domain.model.RoleId;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for {@link RoleController}.
 *
 * @author edmaputra
 */
class RoleControllerTest {

	private ManageRoleUseCase manageRoleUseCase;
	private CurrentActorProvider currentActorProvider;
	private MockMvc mockMvc;

	private final TenantId tenantId = TenantId.generate();

	@BeforeEach
	void setUp() {
		manageRoleUseCase = Mockito.mock(ManageRoleUseCase.class);
		currentActorProvider = Mockito.mock(CurrentActorProvider.class);

		RoleController controller = new RoleController(manageRoleUseCase, currentActorProvider);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new IamExceptionHandler())
				.build();
	}

	@Test
	@DisplayName("POST /api/v1/iam/roles should create custom role and return 201")
	void shouldCreateRole() throws Exception {
		Role role = Role.createCustom(tenantId, "CUSTOM_NURSE", "Nurse Specialist", null, Set.of("READ"));
		when(manageRoleUseCase.createRole(any(CreateRoleCommand.class), any(OperationContext.class))).thenReturn(role);

		String requestJson = """
				{
				    "code": "CUSTOM_NURSE",
				    "name": "Nurse Specialist",
				    "permissions": ["READ"]
				}
				""";

		mockMvc.perform(post("/api/v1/iam/roles")
						.param("tenantId", tenantId.value().toString())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value("CUSTOM_NURSE"))
				.andExpect(jsonPath("$.name").value("Nurse Specialist"));
	}

	@Test
	@DisplayName("GET /api/v1/iam/roles should list roles")
	void shouldListRoles() throws Exception {
		Role role = Role.createCustom(tenantId, "CUSTOM_NURSE", "Nurse Specialist", null, Set.of("READ"));
		when(manageRoleUseCase.listRoles(eq(tenantId), any())).thenReturn(List.of(role));

		mockMvc.perform(get("/api/v1/iam/roles")
						.param("tenantId", tenantId.value().toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("CUSTOM_NURSE"));
	}

	@Test
	@DisplayName("GET /api/v1/iam/roles/{id} should return role by ID")
	void shouldGetRole() throws Exception {
		Role role = Role.createCustom(tenantId, "CUSTOM_NURSE", "Nurse Specialist", null, Set.of("READ"));
		when(manageRoleUseCase.getRoleById(role.getId())).thenReturn(role);

		mockMvc.perform(get("/api/v1/iam/roles/" + role.getId().value()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("CUSTOM_NURSE"));
	}

	@Test
	@DisplayName("PUT /api/v1/iam/roles/{id} should update role")
	void shouldUpdateRole() throws Exception {
		Role role = Role.createCustom(tenantId, "CUSTOM_NURSE", "Updated Nurse", null, Set.of("READ", "WRITE"));
		when(manageRoleUseCase.updateRole(any(UpdateRoleCommand.class), any(OperationContext.class))).thenReturn(role);

		String requestJson = """
				{
				    "name": "Updated Nurse",
				    "permissions": ["READ", "WRITE"]
				}
				""";

		mockMvc.perform(put("/api/v1/iam/roles/" + role.getId().value())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Updated Nurse"));
	}

	@Test
	@DisplayName("DELETE /api/v1/iam/roles/{id} should return 204 No Content")
	void shouldDeleteRole() throws Exception {
		RoleId roleId = RoleId.generate();

		mockMvc.perform(delete("/api/v1/iam/roles/" + roleId.value()))
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("GET /api/v1/iam/permissions should return permissions catalog")
	void shouldListPermissions() throws Exception {
		when(manageRoleUseCase.listAvailablePermissions()).thenReturn(List.of(Permissions.PATIENT_READ, Permissions.PATIENT_WRITE));

		mockMvc.perform(get("/api/v1/iam/permissions"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0]").value(Permissions.PATIENT_READ))
				.andExpect(jsonPath("$[1]").value(Permissions.PATIENT_WRITE));
	}
}
