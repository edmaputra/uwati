package io.github.edmaputra.uwati.iam.application.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.github.edmaputra.uwati.iam.domain.model.ScopeNode;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;

/**
 * Recursive tree node model representing the organizational hierarchy.
 */
public record ScopeTreeNode(
		ScopeNodeId id,
		String code,
		String name,
		String path,
		ScopeNodeId parentId,
		List<ScopeTreeNode> children) {

	public ScopeTreeNode {
		Objects.requireNonNull(id, "ScopeNode ID must not be null.");
		Objects.requireNonNull(code, "Code must not be null.");
		Objects.requireNonNull(name, "Name must not be null.");
		Objects.requireNonNull(path, "Path must not be null.");
		children = children == null ? List.of() : List.copyOf(children);
	}

	public static List<ScopeTreeNode> from(List<ScopeNode> nodes) {
		if (nodes == null || nodes.isEmpty()) {
			return List.of();
		}

		Map<ScopeNodeId, List<ScopeNode>> childrenByParent = new HashMap<>();
		List<ScopeNode> rootNodes = new ArrayList<>();

		for (ScopeNode node : nodes) {
			if (node.isRoot()) {
				rootNodes.add(node);
			} else {
				childrenByParent.computeIfAbsent(node.getParentId(), k -> new ArrayList<>()).add(node);
			}
		}

		List<ScopeTreeNode> result = new ArrayList<>();
		for (ScopeNode root : rootNodes) {
			result.add(buildSubtree(root, childrenByParent));
		}
		return Collections.unmodifiableList(result);
	}

	private static ScopeTreeNode buildSubtree(ScopeNode node, Map<ScopeNodeId, List<ScopeNode>> childrenByParent) {
		List<ScopeNode> childNodes = childrenByParent.getOrDefault(node.getId(), List.of());
		List<ScopeTreeNode> childTreeNodes = new ArrayList<>();
		for (ScopeNode child : childNodes) {
			childTreeNodes.add(buildSubtree(child, childrenByParent));
		}

		return new ScopeTreeNode(
				node.getId(),
				node.getCode(),
				node.getName(),
				node.getPath(),
				node.getParentId(),
				childTreeNodes);
	}
}
