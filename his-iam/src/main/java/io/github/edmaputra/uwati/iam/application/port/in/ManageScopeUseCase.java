package io.github.edmaputra.uwati.iam.application.port.in;

import java.util.List;

import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.application.model.ScopeTreeNode;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNode;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;

/**
 * Inbound port for managing the organizational scope hierarchy.
 */
public interface ManageScopeUseCase {

	ScopeNode createRoot(TenantId tenantId, String code, String name, OperationContext context);

	ScopeNode createChild(TenantId tenantId, ScopeNodeId parentId, String code, String name, OperationContext context);

	ScopeNode updateMetadata(TenantId tenantId, ScopeNodeId id, String code, String name, OperationContext context);

	ScopeNode moveNode(TenantId tenantId, ScopeNodeId id, ScopeNodeId newParentId, OperationContext context);

	void deleteNode(TenantId tenantId, ScopeNodeId id, OperationContext context);

	List<ScopeTreeNode> getScopeTree(TenantId tenantId);

	List<ScopeNode> getFlatScopeList(TenantId tenantId);

	ScopeNode getById(TenantId tenantId, ScopeNodeId id);
}
