package io.github.edmaputra.uwati.domain.audit;

import java.util.Optional;

/**
 * Provides access to the contextual actor and correlation ID for auditing and tracing.
 */
public interface AuditContext {

	Optional<String> currentActor();

	Optional<String> currentCorrelationId();

	default String requireActor() {
		return currentActor().orElse("system");
	}

	default String requireCorrelationId() {
		return currentCorrelationId().orElse("unknown");
	}
}
