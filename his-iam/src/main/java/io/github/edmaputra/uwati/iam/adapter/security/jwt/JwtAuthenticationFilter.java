package io.github.edmaputra.uwati.iam.adapter.security.jwt;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.edmaputra.uwati.domain.security.CurrentActor;
import io.github.edmaputra.uwati.domain.tenancy.application.TenantContextScope;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.adapter.security.SecurityContextAccessor;
import io.github.edmaputra.uwati.iam.domain.exception.AuthenticationException;

/**
 * HTTP filter that extracts the Bearer token from the {@code Authorization} header,
 * verifies it via {@link JwtTokenProvider}, and establishes the request-scoped {@link CurrentActor}
 * and {@link TenantContextScope}.
 *
 * @author edmaputra
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	/** Standard Bearer authorization header prefix. */
	public static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;
	private final SecurityContextAccessor securityContextAccessor;
	private final ObjectProvider<TenantContextScope> tenantContextProvider;

	/**
	 * Constructs the authentication filter with token provider, security context accessor, and optional tenant context provider.
	 *
	 * @param jwtTokenProvider        the token parser and verifier
	 * @param securityContextAccessor the actor context binding accessor
	 * @param tenantContextProvider   the tenant context provider
	 */
	public JwtAuthenticationFilter(
			JwtTokenProvider jwtTokenProvider,
			SecurityContextAccessor securityContextAccessor,
			ObjectProvider<TenantContextScope> tenantContextProvider) {
		this.jwtTokenProvider = Objects.requireNonNull(jwtTokenProvider, "JwtTokenProvider must not be null.");
		this.securityContextAccessor = Objects.requireNonNull(securityContextAccessor, "SecurityContextAccessor must not be null.");
		this.tenantContextProvider = Objects.requireNonNull(tenantContextProvider, "TenantContextProvider must not be null.");
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authHeader.substring(BEARER_PREFIX.length()).trim();
		CurrentActor actor;
		try {
			actor = jwtTokenProvider.parseAccessToken(token);
		}
		catch (AuthenticationException ex) {
			writeUnauthorized(response, ex.getMessage());
			return;
		}
		catch (Exception ex) {
			writeUnauthorized(response, "Invalid or expired authorization token.");
			return;
		}

		try {
			securityContextAccessor.callWithActor(actor, () -> {
				TenantContextScope tenantContext = tenantContextProvider.getIfAvailable();
				if (actor.tenantId() != null && tenantContext != null) {
					tenantContext.callWithTenant(new TenantId(actor.tenantId()), () -> {
						filterChain.doFilter(request, response);
						return null;
					});
				}
				else {
					filterChain.doFilter(request, response);
				}
				return null;
			});
		}
		catch (IOException | ServletException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new ServletException("Security-scoped request execution failed.", ex);
		}
	}

	private void writeUnauthorized(HttpServletResponse response, String detail) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		String json = "{\"type\":\"about:blank\",\"title\":\"Unauthorized\",\"status\":401,\"detail\":\""
				+ escapeJson(detail) + "\"}";
		response.getWriter().write(json);
	}

	private String escapeJson(String input) {
		if (input == null) return "";
		return input.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
	}
}
