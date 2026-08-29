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

	ScopeNode createScopeNode(CreateScopeNodeCommand command, OperationContext context);

	ScopeNode updateMetadata(UpdateScopeNodeCommand command, OperationContext context);

	ScopeNode moveNode(MoveScopeNodeCommand command, OperationContext context);

	void deleteNode(DeleteScopeNodeCommand command, OperationContext context);

	List<ScopeTreeNode> getScopeTree(TenantId tenantId);

	List<ScopeNode> getFlatScopeList(TenantId tenantId);

	ScopeNode getById(TenantId tenantId, ScopeNodeId id);
}
