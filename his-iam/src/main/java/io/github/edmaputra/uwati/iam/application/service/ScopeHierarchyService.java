package io.github.edmaputra.uwati.iam.application.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.application.model.ScopeTreeNode;
import io.github.edmaputra.uwati.iam.application.port.in.ManageScopeUseCase;
import io.github.edmaputra.uwati.iam.domain.event.IamEvent;
import io.github.edmaputra.uwati.iam.domain.event.IamEventTypes;
import io.github.edmaputra.uwati.iam.domain.exception.ScopeNodeNotFoundException;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNode;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;
import io.github.edmaputra.uwati.iam.domain.repository.ScopeNodeRepository;

/**
 * Core application service managing the hierarchical scope tree and lifecycle.
 */
public class ScopeHierarchyService implements ManageScopeUseCase {

	private final ScopeNodeRepository scopeNodeRepository;
	private final ApplicationEventPublisher eventPublisher;

	public ScopeHierarchyService(
			ScopeNodeRepository scopeNodeRepository,
			ApplicationEventPublisher eventPublisher) {
		this.scopeNodeRepository = Objects.requireNonNull(scopeNodeRepository, "ScopeNodeRepository must not be null.");
		this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher must not be null.");
	}

	@Override
	@Transactional
	public ScopeNode createRoot(TenantId tenantId, String code, String name, OperationContext context) {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		validateCodeUniqueness(tenantId, code);

		ScopeNode root = ScopeNode.createRoot(tenantId, code, name);
		ScopeNode saved = scopeNodeRepository.save(root);

		publishEvent(IamEventTypes.SCOPE_NODE_CREATED, tenantId, saved.getId(), saved, context);
		return saved;
	}

	@Override
	@Transactional
	public ScopeNode createChild(TenantId tenantId, ScopeNodeId parentId, String code, String name, OperationContext context) {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		Objects.requireNonNull(parentId, "ParentId must not be null.");
		validateCodeUniqueness(tenantId, code);

		ScopeNode parent = getById(tenantId, parentId);
		ScopeNode child = ScopeNode.createChild(tenantId, parent, code, name);
		ScopeNode saved = scopeNodeRepository.save(child);

		publishEvent(IamEventTypes.SCOPE_NODE_CREATED, tenantId, saved.getId(), saved, context);
		return saved;
	}

	@Override
	@Transactional
	public ScopeNode updateMetadata(TenantId tenantId, ScopeNodeId id, String code, String name, OperationContext context) {
		ScopeNode node = getById(tenantId, id);

		if (!node.getCode().equalsIgnoreCase(code)) {
			validateCodeUniqueness(tenantId, code);
		}

		node.updateMetadata(code, name);
		ScopeNode updated = scopeNodeRepository.save(node);

		publishEvent(IamEventTypes.SCOPE_NODE_UPDATED, tenantId, updated.getId(), updated, context);
		return updated;
	}

	@Override
	@Transactional
	public ScopeNode moveNode(TenantId tenantId, ScopeNodeId id, ScopeNodeId newParentId, OperationContext context) {
		Objects.requireNonNull(id, "Target ScopeNodeId must not be null.");
		Objects.requireNonNull(newParentId, "New parent ScopeNodeId must not be null.");

		if (id.equals(newParentId)) {
			throw new IllegalArgumentException("Cannot move scope node to itself.");
		}

		ScopeNode target = getById(tenantId, id);
		ScopeNode newParent = getById(tenantId, newParentId);

		// Cycle detection: Cannot move a node under one of its own descendants
		if (newParent.getPath().startsWith(target.getPath())) {
			throw new IllegalArgumentException(
					"Cannot move scope node '" + target.getCode() + "' under its descendant '" + newParent.getCode() + "' (cycle detected).");
		}

		String oldPath = target.getPath();
		String newPath = newParent.getPath() + target.getId().value() + "/";

		target.moveTo(newParent.getId(), newPath);
		ScopeNode saved = scopeNodeRepository.save(target);

		// Cascading update to all descendant nodes
		scopeNodeRepository.updatePathPrefix(oldPath, newPath);

		publishEvent(
				IamEventTypes.SCOPE_NODE_MOVED,
				tenantId,
				saved.getId(),
				Map.of("oldPath", oldPath, "newPath", newPath, "newParentId", newParentId.value().toString()),
				context);

		return saved;
	}

	@Override
	@Transactional
	public void deleteNode(TenantId tenantId, ScopeNodeId id, OperationContext context) {
		ScopeNode node = getById(tenantId, id);

		if (scopeNodeRepository.existsByParentId(id)) {
			throw new IllegalStateException("Cannot delete scope node '" + node.getCode() + "' because it has child nodes.");
		}

		scopeNodeRepository.delete(id);
		publishEvent(IamEventTypes.SCOPE_NODE_DELETED, tenantId, id, null, context);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ScopeTreeNode> getScopeTree(TenantId tenantId) {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		List<ScopeNode> nodes = scopeNodeRepository.findAllByTenantId(tenantId);
		return ScopeTreeNode.from(nodes);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ScopeNode> getFlatScopeList(TenantId tenantId) {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		return scopeNodeRepository.findAllByTenantId(tenantId);
	}

	@Override
	@Transactional(readOnly = true)
	public ScopeNode getById(TenantId tenantId, ScopeNodeId id) {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		Objects.requireNonNull(id, "ScopeNodeId must not be null.");

		return scopeNodeRepository.findById(id)
				.filter(n -> n.getTenantId().equals(tenantId))
				.orElseThrow(() -> new ScopeNodeNotFoundException(id));
	}

	private void validateCodeUniqueness(TenantId tenantId, String code) {
		if (scopeNodeRepository.existsByTenantIdAndCode(tenantId, code.trim().toUpperCase())) {
			throw new IllegalArgumentException("A scope node with code '" + code + "' already exists for this tenant.");
		}
	}

	private void publishEvent(
			String eventType,
			TenantId tenantId,
			ScopeNodeId entityId,
			Object payload,
			OperationContext context) {
		IamEvent event = IamEvent.of(
				eventType,
				tenantId.value(),
				entityId.value(),
				"SCOPE_NODE",
				payload,
				context);
		eventPublisher.publishEvent(event);
	}
}
