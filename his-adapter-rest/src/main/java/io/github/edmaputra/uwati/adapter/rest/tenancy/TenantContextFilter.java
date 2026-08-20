package io.github.edmaputra.uwati.adapter.rest.tenancy;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.edmaputra.uwati.domain.tenancy.application.TenantContextScope;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TenantContextFilter extends OncePerRequestFilter {

	public static final String TENANT_ID_HEADER = "X-Tenant-Id";

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
			writeBadRequest(response);
			return;
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
	}

	private void writeBadRequest(HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter()
			.write("{\"code\":\"TENANT_CONTEXT_REQUIRED\",\"message\":\"A valid X-Tenant-Id header is required.\"}");
	}
}
