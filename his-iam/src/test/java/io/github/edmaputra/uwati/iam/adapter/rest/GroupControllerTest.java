package io.github.edmaputra.uwati.iam.adapter.rest;

import java.util.List;
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
import io.github.edmaputra.uwati.iam.application.port.in.AddGroupMemberCommand;
import io.github.edmaputra.uwati.iam.application.port.in.AssignGroupRoleCommand;
import io.github.edmaputra.uwati.iam.application.port.in.CreateGroupCommand;
import io.github.edmaputra.uwati.iam.application.port.in.ManageGroupUseCase;
import io.github.edmaputra.uwati.iam.application.port.in.UpdateGroupCommand;
import io.github.edmaputra.uwati.iam.domain.model.Group;
import io.github.edmaputra.uwati.iam.domain.model.GroupId;
import io.github.edmaputra.uwati.iam.domain.model.GroupRoleAssignment;
import io.github.edmaputra.uwati.iam.domain.model.RoleId;
import io.github.edmaputra.uwati.iam.domain.model.User;
import io.github.edmaputra.uwati.iam.domain.model.UserId;

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
 * Controller tests for {@link GroupController}.
 *
 * @author edmaputra
 */
class GroupControllerTest {

	private ManageGroupUseCase manageGroupUseCase;
	private CurrentActorProvider currentActorProvider;
	private MockMvc mockMvc;

	private final TenantId tenantId = TenantId.generate();

	@BeforeEach
	void setUp() {
		manageGroupUseCase = Mockito.mock(ManageGroupUseCase.class);
		currentActorProvider = Mockito.mock(CurrentActorProvider.class);

		GroupController controller = new GroupController(manageGroupUseCase, currentActorProvider);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new IamExceptionHandler())
				.build();
	}

	@Test
	@DisplayName("POST /api/v1/iam/groups should create group and return 201")
	void shouldCreateGroup() throws Exception {
		Group group = Group.create(tenantId, "DOCTORS", "Medical Doctors", null, null);
		when(manageGroupUseCase.createGroup(any(CreateGroupCommand.class), any(OperationContext.class))).thenReturn(group);

		String requestJson = """
				{
				    "code": "DOCTORS",
				    "name": "Medical Doctors"
				}
				""";

		mockMvc.perform(post("/api/v1/iam/groups")
						.param("tenantId", tenantId.value().toString())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value("DOCTORS"))
				.andExpect(jsonPath("$.name").value("Medical Doctors"));
	}

	@Test
	@DisplayName("GET /api/v1/iam/groups should list groups")
	void shouldListGroups() throws Exception {
		Group group = Group.create(tenantId, "DOCTORS", "Medical Doctors", null, null);
		when(manageGroupUseCase.listGroups(tenantId)).thenReturn(List.of(group));

		mockMvc.perform(get("/api/v1/iam/groups")
						.param("tenantId", tenantId.value().toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("DOCTORS"));
	}

	@Test
	@DisplayName("GET /api/v1/iam/groups/{id} should return group by ID")
	void shouldGetGroup() throws Exception {
		Group group = Group.create(tenantId, "DOCTORS", "Medical Doctors", null, null);
		when(manageGroupUseCase.getGroupById(tenantId, group.getId())).thenReturn(group);

		mockMvc.perform(get("/api/v1/iam/groups/" + group.getId().value())
						.param("tenantId", tenantId.value().toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("DOCTORS"));
	}

	@Test
	@DisplayName("PUT /api/v1/iam/groups/{id} should update group")
	void shouldUpdateGroup() throws Exception {
		Group group = Group.create(tenantId, "DOCTORS", "Updated Doctors", null, null);
		when(manageGroupUseCase.updateGroup(any(UpdateGroupCommand.class), any(OperationContext.class))).thenReturn(group);

		String requestJson = """
				{
				    "name": "Updated Doctors"
				}
				""";

		mockMvc.perform(put("/api/v1/iam/groups/" + group.getId().value())
						.param("tenantId", tenantId.value().toString())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Updated Doctors"));
	}

	@Test
	@DisplayName("DELETE /api/v1/iam/groups/{id} should return 204 No Content")
	void shouldDeleteGroup() throws Exception {
		GroupId groupId = GroupId.generate();

		mockMvc.perform(delete("/api/v1/iam/groups/" + groupId.value())
						.param("tenantId", tenantId.value().toString()))
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("GET /api/v1/iam/groups/{id}/members should list members")
	void shouldListMembers() throws Exception {
		GroupId groupId = GroupId.generate();
		User user = User.create("dr@hospital.org", "pass", "Dr. Doctor", false);
		when(manageGroupUseCase.getGroupMembers(tenantId, groupId)).thenReturn(List.of(user));

		mockMvc.perform(get("/api/v1/iam/groups/" + groupId.value() + "/members")
						.param("tenantId", tenantId.value().toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].email").value("dr@hospital.org"));
	}

	@Test
	@DisplayName("POST /api/v1/iam/groups/{id}/members should add member")
	void shouldAddMember() throws Exception {
		GroupId groupId = GroupId.generate();
		UserId userId = UserId.generate();

		String requestJson = """
				{
				    "userId": "%s"
				}
				""".formatted(userId.value());

		mockMvc.perform(post("/api/v1/iam/groups/" + groupId.value() + "/members")
						.param("tenantId", tenantId.value().toString())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("POST /api/v1/iam/groups/{id}/assignments should assign role to group")
	void shouldAssignRoleToGroup() throws Exception {
		GroupId groupId = GroupId.generate();
		RoleId roleId = RoleId.generate();

		GroupRoleAssignment assignment = GroupRoleAssignment.forTenant(groupId, roleId, tenantId);
		when(manageGroupUseCase.assignRoleToGroup(any(AssignGroupRoleCommand.class), any(OperationContext.class))).thenReturn(assignment);

		String requestJson = """
				{
				    "roleId": "%s"
				}
				""".formatted(roleId.value());

		mockMvc.perform(post("/api/v1/iam/groups/" + groupId.value() + "/assignments")
						.param("tenantId", tenantId.value().toString())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.groupId").value(groupId.value().toString()))
				.andExpect(jsonPath("$.roleId").value(roleId.value().toString()));
	}
}
