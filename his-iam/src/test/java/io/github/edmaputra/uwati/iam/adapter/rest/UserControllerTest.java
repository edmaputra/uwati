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
import io.github.edmaputra.uwati.iam.application.model.EffectiveAccess;
import io.github.edmaputra.uwati.iam.application.port.in.AssignUserRoleCommand;
import io.github.edmaputra.uwati.iam.application.port.in.ChangeUserStatusCommand;
import io.github.edmaputra.uwati.iam.application.port.in.CreateUserCommand;
import io.github.edmaputra.uwati.iam.application.port.in.ManageUserUseCase;
import io.github.edmaputra.uwati.iam.application.port.in.UpdateUserProfileCommand;
import io.github.edmaputra.uwati.iam.domain.model.RoleId;
import io.github.edmaputra.uwati.iam.domain.model.User;
import io.github.edmaputra.uwati.iam.domain.model.UserId;
import io.github.edmaputra.uwati.iam.domain.model.UserRoleAssignment;
import io.github.edmaputra.uwati.iam.domain.model.UserStatus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for {@link UserController}.
 *
 * @author edmaputra
 */
class UserControllerTest {

	private ManageUserUseCase manageUserUseCase;
	private CurrentActorProvider currentActorProvider;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		manageUserUseCase = Mockito.mock(ManageUserUseCase.class);
		currentActorProvider = Mockito.mock(CurrentActorProvider.class);

		UserController controller = new UserController(manageUserUseCase, currentActorProvider);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new IamExceptionHandler())
				.build();
	}

	@Test
	@DisplayName("POST /api/v1/iam/users should create user and return 201")
	void shouldCreateUser() throws Exception {
		User user = User.create("doc@hospital.org", "pass", "Dr. Doctor", false);
		when(manageUserUseCase.createUser(any(CreateUserCommand.class), any(OperationContext.class))).thenReturn(user);

		String requestJson = """
				{
				    "email": "doc@hospital.org",
				    "password": "Password123",
				    "fullName": "Dr. Doctor"
				}
				""";

		mockMvc.perform(post("/api/v1/iam/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("doc@hospital.org"))
				.andExpect(jsonPath("$.fullName").value("Dr. Doctor"))
				.andExpect(jsonPath("$.status").value("ACTIVE"));
	}

	@Test
	@DisplayName("GET /api/v1/iam/users should return list of users")
	void shouldListUsers() throws Exception {
		User user = User.create("doc@hospital.org", "pass", "Dr. Doctor", false);
		when(manageUserUseCase.listUsers(any(), any())).thenReturn(List.of(user));

		mockMvc.perform(get("/api/v1/iam/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].email").value("doc@hospital.org"));
	}

	@Test
	@DisplayName("GET /api/v1/iam/users/{id} should return user detail")
	void shouldGetUserDetail() throws Exception {
		User user = User.create("doc@hospital.org", "pass", "Dr. Doctor", false);
		when(manageUserUseCase.getUserById(user.getId())).thenReturn(user);
		when(manageUserUseCase.getUserRoleAssignments(eq(user.getId()), any())).thenReturn(List.of());
		when(manageUserUseCase.getUserIdentities(user.getId())).thenReturn(List.of());

		mockMvc.perform(get("/api/v1/iam/users/" + user.getId().value()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("doc@hospital.org"))
				.andExpect(jsonPath("$.fullName").value("Dr. Doctor"));
	}

	@Test
	@DisplayName("PUT /api/v1/iam/users/{id} should update user profile")
	void shouldUpdateUserProfile() throws Exception {
		User user = User.create("doc@hospital.org", "pass", "Dr. Doctor Updated", false);
		when(manageUserUseCase.updateUserProfile(any(UpdateUserProfileCommand.class), any(OperationContext.class))).thenReturn(user);

		String requestJson = """
				{
				    "fullName": "Dr. Doctor Updated"
				}
				""";

		mockMvc.perform(put("/api/v1/iam/users/" + user.getId().value())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fullName").value("Dr. Doctor Updated"));
	}

	@Test
	@DisplayName("PATCH /api/v1/iam/users/{id}/status should change user status")
	void shouldChangeUserStatus() throws Exception {
		User user = User.create("doc@hospital.org", "pass", "Dr. Doctor", false);
		user.suspend();
		when(manageUserUseCase.changeUserStatus(any(ChangeUserStatusCommand.class), any(OperationContext.class))).thenReturn(user);

		String requestJson = """
				{
				    "status": "SUSPENDED"
				}
				""";

		mockMvc.perform(patch("/api/v1/iam/users/" + user.getId().value() + "/status")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUSPENDED"));
	}

	@Test
	@DisplayName("DELETE /api/v1/iam/users/{id} should return 204 No Content")
	void shouldDeleteUser() throws Exception {
		UserId userId = UserId.generate();

		mockMvc.perform(delete("/api/v1/iam/users/" + userId.value()))
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("GET /api/v1/iam/users/{id}/effective-access should return effective access summary")
	void shouldGetEffectiveAccess() throws Exception {
		UserId userId = UserId.generate();
		TenantId tenantId = TenantId.generate();

		EffectiveAccess mockAccess = new EffectiveAccess(
				userId,
				"doc@hospital.org",
				tenantId,
				false,
				true,
				Set.of("DOCTORS"),
				Set.of("PHYSICIAN"),
				Set.of("PATIENT_READ"),
				Set.of(),
				Set.of());

		when(manageUserUseCase.getUserEffectiveAccess(eq(userId), eq(tenantId))).thenReturn(mockAccess);

		mockMvc.perform(get("/api/v1/iam/users/" + userId.value() + "/effective-access")
						.param("tenantId", tenantId.value().toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(userId.value().toString()))
				.andExpect(jsonPath("$.roles[0]").value("PHYSICIAN"))
				.andExpect(jsonPath("$.permissions[0]").value("PATIENT_READ"));
	}

	@Test
	@DisplayName("POST /api/v1/iam/users/{userId}/assignments should assign role and return 201")
	void shouldAssignRoleToUser() throws Exception {
		UserId userId = UserId.generate();
		RoleId roleId = RoleId.generate();
		TenantId tenantId = TenantId.generate();

		UserRoleAssignment assignment = UserRoleAssignment.forTenant(userId, roleId, tenantId);
		when(manageUserUseCase.assignRoleToUser(any(AssignUserRoleCommand.class), any(OperationContext.class))).thenReturn(assignment);

		String requestJson = """
				{
				    "roleId": "%s",
				    "tenantId": "%s"
				}
				""".formatted(roleId.value(), tenantId.value());

		mockMvc.perform(post("/api/v1/iam/users/" + userId.value() + "/assignments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.userId").value(userId.value().toString()))
				.andExpect(jsonPath("$.roleId").value(roleId.value().toString()));
	}
}
