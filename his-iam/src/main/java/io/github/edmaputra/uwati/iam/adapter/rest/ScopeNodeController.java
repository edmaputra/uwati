package io.github.edmaputra.uwati.iam.adapter.rest;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.edmaputra.uwati.domain.security.CurrentActorProvider;
import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.adapter.rest.dto.CreateScopeNodeRequest;
import io.github.edmaputra.uwati.iam.adapter.rest.dto.MoveScopeNodeRequest;
import io.github.edmaputra.uwati.iam.adapter.rest.dto.ScopeNodeResponse;
import io.github.edmaputra.uwati.iam.adapter.rest.dto.UpdateScopeNodeRequest;
import io.github.edmaputra.uwati.iam.application.model.ScopeTreeNode;
import io.github.edmaputra.uwati.iam.application.port.in.DeleteScopeNodeCommand;
import io.github.edmaputra.uwati.iam.application.port.in.ManageScopeUseCase;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNode;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;
import jakarta.servlet.http.HttpServletRequest;

/**
 * REST controller exposing organizational scope hierarchy, tree navigation, and scope node CRUD endpoints
 * under {@code /api/v1/iam/scopes}.
 *
 * @author edmaputra
 */
@RestController
@RequestMapping("/api/v1/iam/scopes")
public class ScopeNodeController {

	private final ManageScopeUseCase manageScopeUseCase;
	private final CurrentActorProvider currentActorProvider;

	/**
	 * Constructs the scope node controller.
	 *
	 * @param manageScopeUseCase   the scope management inbound port
	 * @param currentActorProvider the current actor provider
	 */
	public ScopeNodeController(
			ManageScopeUseCase manageScopeUseCase,
			CurrentActorProvider currentActorProvider) {
		this.manageScopeUseCase = Objects.requireNonNull(manageScopeUseCase, "ManageScopeUseCase must not be null.");
		this.currentActorProvider = currentActorProvider;
	}

	/**
	 * Creates a new root or child scope node.
	 *
	 * @param tenantId    the owning tenant ID
	 * @param request     the creation request
	 * @param httpRequest the servlet request
	 * @return HTTP 201 with created {@link ScopeNodeResponse}
	 */
	@PostMapping
	public ResponseEntity<ScopeNodeResponse> createScopeNode(
			@RequestParam String tenantId,
			@RequestBody CreateScopeNodeRequest request,
			HttpServletRequest httpRequest) {
		if (request == null) {
			throw new IllegalArgumentException("Request body must not be null.");
		}
		OperationContext context = OperationContextHelper.resolveContext(httpRequest, currentActorProvider);
		ScopeNode node = manageScopeUseCase.createScopeNode(request.toCommand(TenantId.from(tenantId)), context);
		return ResponseEntity.status(HttpStatus.CREATED).body(ScopeNodeResponse.from(node));
	}

	/**
	 * Retrieves either the nested scope hierarchy tree or a flat list of scope nodes for a tenant.
	 *
	 * @param tenantId the tenant ID
	 * @param tree     whether to return nested tree structure (default: false)
	 * @return HTTP 200 with list of {@link ScopeTreeNode} or {@link ScopeNodeResponse}
	 */
	@GetMapping
	public ResponseEntity<?> getScopes(
			@RequestParam String tenantId,
			@RequestParam(required = false, defaultValue = "false") boolean tree) {
		TenantId tenant = TenantId.from(tenantId);
		if (tree) {
			List<ScopeTreeNode> scopeTree = manageScopeUseCase.getScopeTree(tenant);
			return ResponseEntity.ok(scopeTree);
		}
		List<ScopeNode> flatList = manageScopeUseCase.getFlatScopeList(tenant);
		return ResponseEntity.ok(flatList.stream().map(ScopeNodeResponse::from).toList());
	}

	/**
	 * Retrieves scope node details by ID.
	 *
	 * @param id       the scope node ID
	 * @param tenantId the tenant ID
	 * @return HTTP 200 with {@link ScopeNodeResponse}
	 */
	@GetMapping("/{id}")
	public ResponseEntity<ScopeNodeResponse> getScopeNode(
			@PathVariable String id,
			@RequestParam String tenantId) {
		ScopeNode node = manageScopeUseCase.getById(TenantId.from(tenantId), ScopeNodeId.from(id));
		return ResponseEntity.ok(ScopeNodeResponse.from(node));
	}

	/**
	 * Updates the code and display name of a scope node.
	 *
	 * @param id          the scope node ID
	 * @param tenantId    the tenant ID
	 * @param request     the update request
	 * @param httpRequest the servlet request
	 * @return HTTP 200 with updated {@link ScopeNodeResponse}
	 */
	@PutMapping("/{id}")
	public ResponseEntity<ScopeNodeResponse> updateScopeNode(
			@PathVariable String id,
			@RequestParam String tenantId,
			@RequestBody UpdateScopeNodeRequest request,
			HttpServletRequest httpRequest) {
		if (request == null) {
			throw new IllegalArgumentException("Request body must not be null.");
		}
		OperationContext context = OperationContextHelper.resolveContext(httpRequest, currentActorProvider);
		ScopeNode node = manageScopeUseCase.updateMetadata(request.toCommand(TenantId.from(tenantId), ScopeNodeId.from(id)), context);
		return ResponseEntity.ok(ScopeNodeResponse.from(node));
	}

	/**
	 * Moves/re-parents a scope node and cascades path updates to descendants.
	 *
	 * @param id          the scope node ID
	 * @param tenantId    the tenant ID
	 * @param request     the move request
	 * @param httpRequest the servlet request
	 * @return HTTP 200 with moved {@link ScopeNodeResponse}
	 */
	@PutMapping("/{id}/parent")
	public ResponseEntity<ScopeNodeResponse> moveScopeNode(
			@PathVariable String id,
			@RequestParam String tenantId,
			@RequestBody MoveScopeNodeRequest request,
			HttpServletRequest httpRequest) {
		if (request == null) {
			throw new IllegalArgumentException("Request body must not be null.");
		}
		OperationContext context = OperationContextHelper.resolveContext(httpRequest, currentActorProvider);
		ScopeNode node = manageScopeUseCase.moveNode(request.toCommand(TenantId.from(tenantId), ScopeNodeId.from(id)), context);
		return ResponseEntity.ok(ScopeNodeResponse.from(node));
	}

	/**
	 * Deletes a leaf scope node.
	 *
	 * @param id          the scope node ID
	 * @param tenantId    the tenant ID
	 * @param httpRequest the servlet request
	 * @return HTTP 204 No Content
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteScopeNode(
			@PathVariable String id,
			@RequestParam String tenantId,
			HttpServletRequest httpRequest) {
		OperationContext context = OperationContextHelper.resolveContext(httpRequest, currentActorProvider);
		manageScopeUseCase.deleteNode(new DeleteScopeNodeCommand(TenantId.from(tenantId), ScopeNodeId.from(id)), context);
		return ResponseEntity.noContent().build();
	}
}
