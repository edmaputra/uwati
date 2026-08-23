package io.github.edmaputra.uwati.domain.audit;

import java.lang.ScopedValue;

/**
 * Scoped boundary for executing operations within an actor and correlation-ID context.
 */
public interface AuditContextScope extends AuditContext {

	<T, X extends Throwable> T callWithAuditContext(
			String actor,
			String correlationId,
			ScopedValue.CallableOp<T, X> operation) throws X;
}
