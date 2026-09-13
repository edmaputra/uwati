package io.github.edmaputra.uwati.adapter.rest;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;

public final class OperationContextResolver {

	public static final String ACTOR_HEADER = "X-Actor";
	public static final String ACTOR_ID_HEADER = "X-Actor-Id";
	public static final String USER_ID_HEADER = "X-User-Id";
	public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
	public static final String REQUEST_ID_HEADER = "X-Request-Id";

	private OperationContextResolver() {
	}

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
