package io.github.edmaputra.uwati.domain.tenancy.application;

import java.util.Objects;
import java.util.Optional;

/**
 * Cross-cutting operation context that carries the identity of the actor
 * performing the action and an optional correlation ID for request tracing.
 *
 * @author edmaputra
 */
public record OperationContext(String actor, String correlationId) {

	public OperationContext {
		Objects.requireNonNull(actor, "Actor must not be null.");
		if (actor.isBlank()) {
			throw new IllegalArgumentException("Actor must not be blank.");
		}
	}

	public Optional<String> optionalCorrelationId() {
		return Optional.ofNullable(correlationId);
	}

	public static OperationContext of(String actor, String correlationId) {
		return new OperationContext(actor, correlationId);
	}

	public static OperationContext of(String actor) {
		return new OperationContext(actor, null);
	}

	public static OperationContext system() {
		return new OperationContext("system", null);
	}
}
