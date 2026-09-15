package io.github.edmaputra.uwati.adapter.rest;

import java.util.Locale;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

import io.github.edmaputra.iam.domain.context.ActorType;
import io.github.edmaputra.iam.domain.context.OperationContext;

/**
 * Utility for resolving the operational audit context from HTTP request headers.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public final class OperationContextResolver {

	public static final String ACTOR_HEADER = "X-Actor";
	public static final String ACTOR_ID_HEADER = "X-Actor-Id";
	public static final String ACTOR_TYPE_HEADER = "X-Actor-Type";
	public static final String USER_ID_HEADER = "X-User-Id";
	public static final String TENANT_ID_HEADER = "X-Tenant-Id";
	public static final String TENANT_ID_HEADER_ALT = "X-Tenant-ID";
	public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
	public static final String REQUEST_ID_HEADER = "X-Request-Id";

	private OperationContextResolver() {
	}

	/**
	 * Resolves the operational context from the incoming HTTP request.
	 *
	 * @param request the HTTP servlet request
	 * @return resolved operation context with actor, actor type, optional tenant ID, and correlation ID
	 */
	public static OperationContext resolve(HttpServletRequest request) {
		String actor = request.getHeader(ACTOR_ID_HEADER);
		if (actor == null || actor.isBlank()) {
			actor = request.getHeader(ACTOR_HEADER);
		}
		if (actor == null || actor.isBlank()) {
			actor = request.getHeader(USER_ID_HEADER);
		}
		if (actor == null || actor.isBlank()) {
			actor = "system";
		}

		String actorTypeHeader = request.getHeader(ACTOR_TYPE_HEADER);
		ActorType actorType = resolveActorType(actorTypeHeader, actor);

		UUID tenantUuid = resolveTenantId(request);

		String correlationId = request.getHeader(CORRELATION_ID_HEADER);
		if (correlationId == null || correlationId.isBlank()) {
			correlationId = request.getHeader(REQUEST_ID_HEADER);
		}
		if (correlationId == null || correlationId.isBlank()) {
			correlationId = UUID.randomUUID().toString();
		}

		return OperationContext.of(actor.trim(), actorType, tenantUuid, correlationId.trim());
	}

	private static ActorType resolveActorType(String actorTypeHeader, String actor) {
		if (actorTypeHeader != null && !actorTypeHeader.isBlank()) {
			try {
				return ActorType.valueOf(actorTypeHeader.trim().toUpperCase(Locale.ROOT));
			}
			catch (IllegalArgumentException ignored) {
				// Fall through to infer from actor identity
			}
		}
		if ("system".equalsIgnoreCase(actor.trim())) {
			return ActorType.SYSTEM;
		}
		return ActorType.USER;
	}

	private static UUID resolveTenantId(HttpServletRequest request) {
		String tenantHeader = request.getHeader(TENANT_ID_HEADER);
		if (tenantHeader == null || tenantHeader.isBlank()) {
			tenantHeader = request.getHeader(TENANT_ID_HEADER_ALT);
		}
		if (tenantHeader != null && !tenantHeader.isBlank()) {
			try {
				return UUID.fromString(tenantHeader.trim());
			}
			catch (IllegalArgumentException ignored) {
				// Ignored if invalid UUID, leave tenantUuid null
			}
		}
		return null;
	}
}
