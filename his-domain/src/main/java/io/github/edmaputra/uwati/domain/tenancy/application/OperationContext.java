package io.github.edmaputra.uwati.domain.tenancy.application;

import java.util.Objects;
import java.util.Optional;

/**
 * Cross-cutting operation context that carries the identity of the actor
 * performing the action and an optional correlation ID for request tracing.
 * <p>
 * Serves as an application-layer context object in the Hexagonal Architecture passed into
 * use case boundaries for auditing and traceability.
 *
 * @param actor the identifier of the authenticated actor executing the operation
 * @param correlationId the correlation identifier for request tracking, may be {@code null}
 * @author edmaputra
 * @since 0.0.1
 */
public record OperationContext(String actor, String correlationId) {

	/**
	 * Compact constructor validating that the actor is present and non-blank.
	 *
	 * @param actor the actor identifier
	 * @param correlationId the correlation identifier
	 * @throws NullPointerException if {@code actor} is null
	 * @throws IllegalArgumentException if {@code actor} is blank
	 */
	public OperationContext {
		Objects.requireNonNull(actor, "Actor must not be null.");
		if (actor.isBlank()) {
			throw new IllegalArgumentException("Actor must not be blank.");
		}
	}

	/**
	 * Returns an {@link Optional} describing the correlation ID, if present.
	 *
	 * @return an optional containing the correlation ID, or empty if null
	 */
	public Optional<String> optionalCorrelationId() {
		return Optional.ofNullable(correlationId);
	}

	/**
	 * Creates an {@link OperationContext} with both an actor and correlation ID.
	 *
	 * @param actor the actor executing the operation
	 * @param correlationId the correlation identifier
	 * @return a new operation context instance
	 */
	public static OperationContext of(String actor, String correlationId) {
		return new OperationContext(actor, correlationId);
	}

	/**
	 * Creates an {@link OperationContext} with an actor and no correlation ID.
	 *
	 * @param actor the actor executing the operation
	 * @return a new operation context instance
	 */
	public static OperationContext of(String actor) {
		return new OperationContext(actor, null);
	}

	/**
	 * Creates a default system-level {@link OperationContext}.
	 *
	 * @return a new operation context for background or system tasks
	 */
	public static OperationContext system() {
		return new OperationContext("system", null);
	}
}
