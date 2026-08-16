package io.github.edmaputra.uwati.adapter.rest.tenancy;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.edmaputra.uwati.core.tenancy.application.TenantContextScope;
import io.github.edmaputra.uwati.core.tenancy.domain.TenantId;

@Component
public class TenantContextFilter extends OncePerRequestFilter {

	public static final String TENANT_ID_HEADER = "X-Tenant-Id";

	private final TenantContextScope tenantContext;

	public TenantContextFilter(TenantContextScope tenantContext) {
		this.tenantContext = tenantContext;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !request.getRequestURI().startsWith("/api/");
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

		try (TenantContextScope.Scope ignored = tenantContext.open(tenantId)) {
			filterChain.doFilter(request, response);
		}
	}

	private void writeBadRequest(HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter()
			.write("{\"code\":\"TENANT_CONTEXT_REQUIRED\",\"message\":\"A valid X-Tenant-Id header is required.\"}");
	}
}
