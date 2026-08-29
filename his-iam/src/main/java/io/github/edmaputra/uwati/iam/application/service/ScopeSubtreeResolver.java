package io.github.edmaputra.uwati.iam.application.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNode;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;
import io.github.edmaputra.uwati.iam.domain.repository.ScopeNodeRepository;

/**
 * High-performance subtree resolver for computing accessible scope nodes and evaluating path prefixes.
 */
public class ScopeSubtreeResolver {

	private final ScopeNodeRepository scopeNodeRepository;

	public ScopeSubtreeResolver(ScopeNodeRepository scopeNodeRepository) {
		this.scopeNodeRepository = Objects.requireNonNull(scopeNodeRepository, "ScopeNodeRepository must not be null.");
	}

	/**
	 * Resolves the complete set of ScopeNodeIds accessible given a collection of directly assigned scope node IDs.
	 * Includes each assigned node and all of its transitive descendants.
	 */
	public Set<ScopeNodeId> resolveAccessibleScopeNodeIds(TenantId tenantId, Collection<ScopeNodeId> assignedScopeNodeIds) {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		if (assignedScopeNodeIds == null || assignedScopeNodeIds.isEmpty()) {
			return Set.of();
		}

		Set<ScopeNodeId> accessibleIds = new HashSet<>();

		for (ScopeNodeId assignedId : assignedScopeNodeIds) {
			scopeNodeRepository.findById(assignedId).ifPresent(node -> {
				if (node.getTenantId().equals(tenantId)) {
					accessibleIds.add(node.getId());
					List<ScopeNode> descendants = scopeNodeRepository.findDescendantsByPathPrefix(node.getPath());
					for (ScopeNode descendant : descendants) {
						accessibleIds.add(descendant.getId());
					}
				}
			});
		}

		return Collections.unmodifiableSet(accessibleIds);
	}

	/**
	 * Checks whether a target ScopeNodeId is accessible given a collection of assigned scope node IDs.
	 */
	public boolean isScopeAccessible(TenantId tenantId, Collection<ScopeNodeId> assignedScopeNodeIds, ScopeNodeId targetScopeNodeId) {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		Objects.requireNonNull(targetScopeNodeId, "Target ScopeNodeId must not be null.");

		if (assignedScopeNodeIds == null || assignedScopeNodeIds.isEmpty()) {
			return false;
		}

		if (assignedScopeNodeIds.contains(targetScopeNodeId)) {
			return true;
		}

		return scopeNodeRepository.findById(targetScopeNodeId)
				.filter(target -> target.getTenantId().equals(tenantId))
				.map(target -> isPathAccessibleForNodes(assignedScopeNodeIds, target.getPath()))
				.orElse(false);
	}

	/**
	 * Fast memory-only prefix check: returns true if targetPath is covered by any of the assigned paths.
	 */
	public static boolean isPathAccessible(Collection<String> assignedPaths, String targetPath) {
		if (assignedPaths == null || assignedPaths.isEmpty() || targetPath == null || targetPath.isBlank()) {
			return false;
		}

		for (String assignedPath : assignedPaths) {
			if (targetPath.startsWith(assignedPath)) {
				return true;
			}
		}
		return false;
	}

	private boolean isPathAccessibleForNodes(Collection<ScopeNodeId> assignedScopeNodeIds, String targetPath) {
		for (ScopeNodeId assignedId : assignedScopeNodeIds) {
			ScopeNode assignedNode = scopeNodeRepository.findById(assignedId).orElse(null);
			if (assignedNode != null && targetPath.startsWith(assignedNode.getPath())) {
				return true;
			}
		}
		return false;
	}
}
