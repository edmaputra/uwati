package io.github.edmaputra.uwati.domain.security;

import java.util.UUID;

/**
 * Marks a domain entity or aggregate as belonging to an organizational unit or hierarchical Scope Node.
 *
 * @author edmaputra
 */
public interface ScopeOwned {

	/**
	 * Returns the UUID of the owning scope node.
	 *
	 * @return scope node UUID
	 */
	UUID scopeNodeId();
}
