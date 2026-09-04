package io.github.edmaputra.uwati.iam.adapter.rest;

import java.util.UUID;

import io.github.edmaputra.uwati.domain.security.CurrentActor;
import io.github.edmaputra.uwati.domain.security.CurrentActorProvider;
import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility helper for resolving {@link OperationContext} from HTTP request headers and security context.
 *
 * @author edmaputra
 */
public final class OperationContextHelper {

	public static final String ACTOR_HEADER = "X-Actor";
	public static final String ACTOR_ID_HEADER = "X-Actor-Id";
	public static final String USER_ID_HEADER = "X-User-Id";
	public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
	public static final String REQUEST_ID_HEADER = "X-Request-Id";

	private OperationContextHelper() {}

	/**
	 * Resolves the operation context from request headers or security context.
	 *
	 * @param request              the HTTP servlet request
	 * @param currentActorProvider the current actor provider (optional)
	 * @return the populated {@link OperationContext}
	 */
	public static OperationContext resolveContext(
			HttpServletRequest request,
			CurrentActorProvider currentActorProvider) {
		String actor = null;
		if (currentActorProvider != null) {
			actor = currentActorProvider.currentActor()
					.map(CurrentActor::email)
					.orElse(null);
		}

		if (actor == null || actor.isBlank()) {
			actor = request.getHeader(ACTOR_ID_HEADER);
		}
		if (actor == null || actor.isBlank()) {
			actor = request.getHeader(ACTOR_HEADER);
		}
		if (actor == null || actor.isBlank()) {
			actor = request.getHeader(USER_ID_HEADER);
		}
		if (actor == null || actor.isBlank()) {
			actor = "system";
		}

		String correlationId = request.getHeader(CORRELATION_ID_HEADER);
		if (correlationId == null || correlationId.isBlank()) {
			correlationId = request.getHeader(REQUEST_ID_HEADER);
		}
		if (correlationId == null || correlationId.isBlank()) {
			correlationId = UUID.randomUUID().toString();
		}

		return OperationContext.of(actor.trim(), correlationId.trim());
	}
}
