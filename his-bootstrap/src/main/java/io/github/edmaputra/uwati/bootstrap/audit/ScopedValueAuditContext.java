package io.github.edmaputra.uwati.bootstrap.audit;

import java.lang.ScopedValue;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import io.github.edmaputra.uwati.domain.audit.AuditContextScope;

/**
 * ScopedValue-based implementation of AuditContextScope.
 */
@Component
public class ScopedValueAuditContext implements AuditContextScope {

	private static final ScopedValue<String> ACTOR = ScopedValue.newInstance();
	private static final ScopedValue<String> CORRELATION_ID = ScopedValue.newInstance();

	@Override
	public Optional<String> currentActor() {
		return ACTOR.isBound() ? Optional.of(ACTOR.get()) : Optional.empty();
	}

	@Override
	public Optional<String> currentCorrelationId() {
		return CORRELATION_ID.isBound() ? Optional.of(CORRELATION_ID.get()) : Optional.empty();
	}

	@Override
	public <T, X extends Throwable> T callWithAuditContext(
			String actor,
			String correlationId,
			ScopedValue.CallableOp<T, X> operation) throws X {

		Objects.requireNonNull(operation, "Audit operation must not be null.");

		String effectiveActor = actor != null && !actor.isBlank() ? actor : "system";
		String effectiveCorrelationId = correlationId != null && !correlationId.isBlank() ? correlationId : "unknown";

		return ScopedValue
				.where(ACTOR, effectiveActor)
				.where(CORRELATION_ID, effectiveCorrelationId)
				.call(operation);
	}
}
