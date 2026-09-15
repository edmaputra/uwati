package io.github.edmaputra.uwati.adapter.rest;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import io.github.edmaputra.iam.domain.context.ActorType;
import io.github.edmaputra.iam.domain.context.OperationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link OperationContextResolver}.
 *
 * @author edmaputra
 * @since 0.0.1
 */
class OperationContextResolverTest {

	@Test
	void shouldResolveDefaultContextWhenNoHeadersProvided() {
		MockHttpServletRequest request = new MockHttpServletRequest();

		OperationContext context = OperationContextResolver.resolve(request);

		assertThat(context.actor()).isEqualTo("system");
		assertThat(context.actorType()).isEqualTo(ActorType.SYSTEM);
		assertThat(context.optionalTenantId()).isEmpty();
		assertThat(context.optionalTenantUuid()).isEmpty();
		assertThat(context.correlationId()).isNotBlank();
	}

	@Test
	void shouldResolveUserContextFromActorIdHeader() {
		UUID tenantId = UUID.randomUUID();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(OperationContextResolver.ACTOR_ID_HEADER, "dr-jane");
		request.addHeader(OperationContextResolver.TENANT_ID_HEADER, tenantId.toString());
		request.addHeader(OperationContextResolver.CORRELATION_ID_HEADER, "corr-12345");

		OperationContext context = OperationContextResolver.resolve(request);

		assertThat(context.actor()).isEqualTo("dr-jane");
		assertThat(context.actorType()).isEqualTo(ActorType.USER);
		assertThat(context.optionalTenantUuid()).contains(tenantId);
		assertThat(context.correlationId()).isEqualTo("corr-12345");
	}

	@Test
	void shouldResolveMachineContextFromActorTypeHeader() {
		UUID tenantId = UUID.randomUUID();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(OperationContextResolver.ACTOR_HEADER, "integration-service");
		request.addHeader(OperationContextResolver.ACTOR_TYPE_HEADER, "MACHINE");
		request.addHeader(OperationContextResolver.TENANT_ID_HEADER_ALT, tenantId.toString());
		request.addHeader(OperationContextResolver.REQUEST_ID_HEADER, "req-999");

		OperationContext context = OperationContextResolver.resolve(request);

		assertThat(context.actor()).isEqualTo("integration-service");
		assertThat(context.actorType()).isEqualTo(ActorType.MACHINE);
		assertThat(context.optionalTenantUuid()).contains(tenantId);
		assertThat(context.correlationId()).isEqualTo("req-999");
	}

	@Test
	void shouldInferSystemActorTypeWhenActorIsSystem() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(OperationContextResolver.USER_ID_HEADER, "system");

		OperationContext context = OperationContextResolver.resolve(request);

		assertThat(context.actor()).isEqualTo("system");
		assertThat(context.actorType()).isEqualTo(ActorType.SYSTEM);
	}

	@Test
	void shouldHandleInvalidActorTypeGracefully() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(OperationContextResolver.ACTOR_ID_HEADER, "nurse-bob");
		request.addHeader(OperationContextResolver.ACTOR_TYPE_HEADER, "INVALID_TYPE");

		OperationContext context = OperationContextResolver.resolve(request);

		assertThat(context.actor()).isEqualTo("nurse-bob");
		assertThat(context.actorType()).isEqualTo(ActorType.USER);
	}

	@Test
	void shouldHandleInvalidTenantIdGracefully() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(OperationContextResolver.ACTOR_ID_HEADER, "admin");
		request.addHeader(OperationContextResolver.TENANT_ID_HEADER, "not-a-valid-uuid");

		OperationContext context = OperationContextResolver.resolve(request);

		assertThat(context.actor()).isEqualTo("admin");
		assertThat(context.optionalTenantUuid()).isEmpty();
	}
}
