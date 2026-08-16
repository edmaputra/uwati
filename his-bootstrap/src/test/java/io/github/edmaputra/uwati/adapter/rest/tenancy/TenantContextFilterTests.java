package io.github.edmaputra.uwati.adapter.rest.tenancy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import io.github.edmaputra.uwati.bootstrap.tenancy.ThreadLocalTenantContext;
import io.github.edmaputra.uwati.core.tenancy.domain.TenantId;

class TenantContextFilterTests {

	@Test
	void rejectsApiRequestsWithoutATenantHeader() throws Exception {
		TenantContextFilter filter = new TenantContextFilter(new ThreadLocalTenantContext());
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/patients");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> {
			throw new AssertionError("The filter chain must not be called.");
		});

		assertThat(response.getStatus()).isEqualTo(400);
		assertThat(response.getContentAsString()).contains("TENANT_CONTEXT_REQUIRED");
	}

	@Test
	void makesTheHeaderTenantAvailableOnlyDuringTheApiRequest() throws Exception {
		ThreadLocalTenantContext context = new ThreadLocalTenantContext();
		TenantContextFilter filter = new TenantContextFilter(context);
		TenantId tenantId = TenantId.generate();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/patients");
		request.addHeader(TenantContextFilter.TENANT_ID_HEADER, tenantId.toString());
		MockHttpServletResponse response = new MockHttpServletResponse();
		AtomicReference<TenantId> observedTenantId = new AtomicReference<>();

		filter.doFilter(request, response,
				(ignoredRequest, ignoredResponse) -> observedTenantId.set(context.requireTenantId()));

		assertThat(observedTenantId.get()).isEqualTo(tenantId);
		assertThat(context.currentTenantId()).isEmpty();
	}
}
