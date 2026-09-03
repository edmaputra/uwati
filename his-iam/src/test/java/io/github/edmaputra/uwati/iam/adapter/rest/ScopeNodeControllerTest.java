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
import io.github.edmaputra.uwati.iam.application.model.ScopeTreeNode;
import io.github.edmaputra.uwati.iam.application.port.in.CreateScopeNodeCommand;
import io.github.edmaputra.uwati.iam.application.port.in.DeleteScopeNodeCommand;
import io.github.edmaputra.uwati.iam.application.port.in.ManageScopeUseCase;
import io.github.edmaputra.uwati.iam.application.port.in.MoveScopeNodeCommand;
import io.github.edmaputra.uwati.iam.application.port.in.UpdateScopeNodeCommand;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNode;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for {@link ScopeNodeController}.
 *
 * @author edmaputra
 */
class ScopeNodeControllerTest {

	private ManageScopeUseCase manageScopeUseCase;
	private CurrentActorProvider currentActorProvider;
	private MockMvc mockMvc;

	private final TenantId tenantId = TenantId.generate();

	@BeforeEach
	void setUp() {
		manageScopeUseCase = Mockito.mock(ManageScopeUseCase.class);
		currentActorProvider = Mockito.mock(CurrentActorProvider.class);

		ScopeNodeController controller = new ScopeNodeController(manageScopeUseCase, currentActorProvider);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new IamExceptionHandler())
				.build();
	}

	@Test
	@DisplayName("POST /api/v1/iam/scopes should create scope node and return 201")
	void shouldCreateScopeNode() throws Exception {
		ScopeNode node = ScopeNode.createRoot(tenantId, "MAIN", "Main Facility");
		when(manageScopeUseCase.createScopeNode(any(CreateScopeNodeCommand.class), any(OperationContext.class))).thenReturn(node);

		String requestJson = """
				{
				    "code": "MAIN",
				    "name": "Main Facility"
				}
				""";

		mockMvc.perform(post("/api/v1/iam/scopes")
						.param("tenantId", tenantId.value().toString())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value("MAIN"))
				.andExpect(jsonPath("$.name").value("Main Facility"));
	}

	@Test
	@DisplayName("GET /api/v1/iam/scopes (flat list) should return flat nodes")
	void shouldGetFlatScopes() throws Exception {
		ScopeNode node = ScopeNode.createRoot(tenantId, "MAIN", "Main Facility");
		when(manageScopeUseCase.getFlatScopeList(tenantId)).thenReturn(List.of(node));

		mockMvc.perform(get("/api/v1/iam/scopes")
						.param("tenantId", tenantId.value().toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("MAIN"));
	}

	@Test
	@DisplayName("GET /api/v1/iam/scopes?tree=true should return tree nodes")
	void shouldGetScopeTree() throws Exception {
		ScopeNode node = ScopeNode.createRoot(tenantId, "MAIN", "Main Facility");
		List<ScopeTreeNode> treeNodes = ScopeTreeNode.from(List.of(node));
		when(manageScopeUseCase.getScopeTree(tenantId)).thenReturn(treeNodes);

		mockMvc.perform(get("/api/v1/iam/scopes")
						.param("tenantId", tenantId.value().toString())
						.param("tree", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("MAIN"));
	}

	@Test
	@DisplayName("GET /api/v1/iam/scopes/{id} should return scope node by ID")
	void shouldGetScopeNode() throws Exception {
		ScopeNode node = ScopeNode.createRoot(tenantId, "MAIN", "Main Facility");
		when(manageScopeUseCase.getById(tenantId, node.getId())).thenReturn(node);

		mockMvc.perform(get("/api/v1/iam/scopes/" + node.getId().value())
						.param("tenantId", tenantId.value().toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("MAIN"));
	}

	@Test
	@DisplayName("PUT /api/v1/iam/scopes/{id} should update scope node metadata")
	void shouldUpdateScopeNode() throws Exception {
		ScopeNode node = ScopeNode.createRoot(tenantId, "MAIN", "Updated Facility");
		when(manageScopeUseCase.updateMetadata(any(UpdateScopeNodeCommand.class), any(OperationContext.class))).thenReturn(node);

		String requestJson = """
				{
				    "code": "MAIN",
				    "name": "Updated Facility"
				}
				""";

		mockMvc.perform(put("/api/v1/iam/scopes/" + node.getId().value())
						.param("tenantId", tenantId.value().toString())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Updated Facility"));
	}

	@Test
	@DisplayName("PUT /api/v1/iam/scopes/{id}/parent should move scope node")
	void shouldMoveScopeNode() throws Exception {
		ScopeNode node = ScopeNode.createRoot(tenantId, "WARD1", "Ward 1");
		when(manageScopeUseCase.moveNode(any(MoveScopeNodeCommand.class), any(OperationContext.class))).thenReturn(node);

		String requestJson = """
				{
				    "newParentId": "%s"
				}
				""".formatted(UUID.randomUUID());

		mockMvc.perform(put("/api/v1/iam/scopes/" + node.getId().value() + "/parent")
						.param("tenantId", tenantId.value().toString())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("WARD1"));
	}

	@Test
	@DisplayName("DELETE /api/v1/iam/scopes/{id} should return 204 No Content")
	void shouldDeleteScopeNode() throws Exception {
		ScopeNodeId nodeId = ScopeNodeId.generate();

		mockMvc.perform(delete("/api/v1/iam/scopes/" + nodeId.value())
						.param("tenantId", tenantId.value().toString()))
				.andExpect(status().isNoContent());
	}
}
