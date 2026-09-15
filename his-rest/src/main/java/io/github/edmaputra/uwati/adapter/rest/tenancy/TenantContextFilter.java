package io.github.edmaputra.uwati.adapter.rest.tenancy;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.edmaputra.uwati.domain.tenancy.application.TenantContextScope;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import lombok.RequiredArgsConstructor;

/**
 * Servlet filter establishing tenant boundary and diagnostic context (MDC) for incoming API requests.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class TenantContextFilter extends OncePerRequestFilter {

	public static final String TENANT_ID_HEADER = "X-Tenant-Id";
	public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
	public static final String REQUEST_ID_HEADER = "X-Request-Id";

	private final TenantContextScope tenantContext;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String requestUri = request.getRequestURI();
		return !requestUri.startsWith("/api/") || requestUri.startsWith("/api/platform/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		TenantId tenantId;
		try {
			tenantId = TenantId.from(request.getHeader(TENANT_ID_HEADER));
		}
		catch (IllegalArgumentException exception) {
			writeBadRequest(request, response);
			return;
		}

		String correlationId = request.getHeader(CORRELATION_ID_HEADER);
		if (correlationId == null || correlationId.isBlank()) {
			correlationId = request.getHeader(REQUEST_ID_HEADER);
		}

		MDC.put("tenantId", tenantId.value().toString());
		if (correlationId != null && !correlationId.isBlank()) {
			MDC.put("requestId", correlationId.trim());
		}

		try {
			tenantContext.callWithTenant(tenantId, () -> {
				filterChain.doFilter(request, response);
				return null;
			});
		}
		catch (IOException | ServletException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new ServletException("Tenant-scoped request execution failed.", exception);
		}
		finally {
			MDC.remove("tenantId");
			if (correlationId != null && !correlationId.isBlank()) {
				MDC.remove("requestId");
			}
		}
	}

	private void writeBadRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		response.setContentType("application/problem+json");
		String json = """
				{"type":"https://api.uwati.example.com/problems/tenant-context-required","title":"Tenant Context Required","status":400,"detail":"A valid X-Tenant-Id header is required.","instance":"%s","code":"TENANT_CONTEXT_REQUIRED"}"""
				.formatted(request.getRequestURI());
		response.getWriter().write(json);
	}
}
