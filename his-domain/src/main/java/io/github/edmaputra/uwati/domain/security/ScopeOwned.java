package io.github.edmaputra.uwati.domain.security;

import java.util.UUID;

/**
 * Marks a domain entity or aggregate as belonging to an organizational unit (Scope Node).
 */
public interface ScopeOwned {

	UUID scopeNodeId();
}
