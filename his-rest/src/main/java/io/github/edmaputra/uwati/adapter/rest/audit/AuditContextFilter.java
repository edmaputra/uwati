package io.github.edmaputra.uwati.adapter.rest.audit;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.edmaputra.uwati.domain.audit.AuditContextScope;
import lombok.RequiredArgsConstructor;

/**
 * Filter that establishes contextual actor and correlation ID for each incoming HTTP request.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class AuditContextFilter extends OncePerRequestFilter {

	public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
	public static final String REQUEST_ID_HEADER = "X-Request-Id";
	public static final String ACTOR_ID_HEADER = "X-Actor-Id";
	public static final String USER_ID_HEADER = "X-User-Id";

	public static final String MDC_CORRELATION_ID = "correlationId";
	public static final String MDC_ACTOR = "actor";

	private final AuditContextScope auditContext;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String correlationId = resolveCorrelationId(request);
		String actor = resolveActor(request);

		response.setHeader(CORRELATION_ID_HEADER, correlationId);

		MDC.put(MDC_CORRELATION_ID, correlationId);
		MDC.put(MDC_ACTOR, actor);

		try {
			auditContext.callWithAuditContext(actor, correlationId, () -> {
				filterChain.doFilter(request, response);
				return null;
			});
		}
		catch (IOException | ServletException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new ServletException("Audit-scoped request execution failed.", exception);
		}
		finally {
			MDC.remove(MDC_CORRELATION_ID);
			MDC.remove(MDC_ACTOR);
		}
	}

	private String resolveCorrelationId(HttpServletRequest request) {
		String correlationId = request.getHeader(CORRELATION_ID_HEADER);
		if (correlationId == null || correlationId.isBlank()) {
			correlationId = request.getHeader(REQUEST_ID_HEADER);
		}
		if (correlationId == null || correlationId.isBlank()) {
			correlationId = UUID.randomUUID().toString();
		}
		return correlationId.trim();
	}

	private String resolveActor(HttpServletRequest request) {
		String actor = request.getHeader(ACTOR_ID_HEADER);
		if (actor == null || actor.isBlank()) {
			actor = request.getHeader(USER_ID_HEADER);
		}
		if (actor == null || actor.isBlank()) {
			actor = "anonymous";
		}
		return actor.trim();
	}
}
