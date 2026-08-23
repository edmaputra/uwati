package io.github.edmaputra.uwati.adapter.rest.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import io.github.edmaputra.uwati.bootstrap.audit.ScopedValueAuditContext;

@DisplayName("AuditContextFilter Unit Tests")
class AuditContextFilterTests {

	private final ScopedValueAuditContext auditContext = new ScopedValueAuditContext();
	private final AuditContextFilter filter = new AuditContextFilter(auditContext);

	@Test
	@DisplayName("propagates correlation-id and actor headers and adds correlation-id to response")
	void propagatesCorrelationIdAndActorHeaders() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/platform/tenants");
		request.addHeader(AuditContextFilter.CORRELATION_ID_HEADER, "custom-corr-123");
		request.addHeader(AuditContextFilter.ACTOR_ID_HEADER, "operator-alice");

		MockHttpServletResponse response = new MockHttpServletResponse();

		AtomicReference<String> observedActor = new AtomicReference<>();
		AtomicReference<String> observedCorrelationId = new AtomicReference<>();
		AtomicReference<String> observedMdcActor = new AtomicReference<>();
		AtomicReference<String> observedMdcCorrelationId = new AtomicReference<>();

		filter.doFilter(request, response, (req, res) -> {
			observedActor.set(auditContext.requireActor());
			observedCorrelationId.set(auditContext.requireCorrelationId());
			observedMdcActor.set(MDC.get(AuditContextFilter.MDC_ACTOR));
			observedMdcCorrelationId.set(MDC.get(AuditContextFilter.MDC_CORRELATION_ID));
		});

		assertThat(observedActor.get()).isEqualTo("operator-alice");
		assertThat(observedCorrelationId.get()).isEqualTo("custom-corr-123");
		assertThat(observedMdcActor.get()).isEqualTo("operator-alice");
		assertThat(observedMdcCorrelationId.get()).isEqualTo("custom-corr-123");

		assertThat(response.getHeader(AuditContextFilter.CORRELATION_ID_HEADER)).isEqualTo("custom-corr-123");

		// MDC and ScopedValue are cleared after filter finishes
		assertThat(MDC.get(AuditContextFilter.MDC_ACTOR)).isNull();
		assertThat(MDC.get(AuditContextFilter.MDC_CORRELATION_ID)).isNull();
		assertThat(auditContext.currentActor()).isEmpty();
		assertThat(auditContext.currentCorrelationId()).isEmpty();
	}

	@Test
	@DisplayName("generates UUID correlation ID and defaults actor to anonymous when headers are missing")
	void generatesDefaultsWhenHeadersMissing() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/patients");
		MockHttpServletResponse response = new MockHttpServletResponse();

		AtomicReference<String> observedActor = new AtomicReference<>();
		AtomicReference<String> observedCorrelationId = new AtomicReference<>();

		filter.doFilter(request, response, (req, res) -> {
			observedActor.set(auditContext.requireActor());
			observedCorrelationId.set(auditContext.requireCorrelationId());
		});

		assertThat(observedActor.get()).isEqualTo("anonymous");
		assertThat(observedCorrelationId.get()).isNotBlank();
		assertThat(response.getHeader(AuditContextFilter.CORRELATION_ID_HEADER)).isEqualTo(observedCorrelationId.get());
	}
}
