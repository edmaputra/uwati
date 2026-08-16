package io.github.edmaputra.uwati.bootstrap.tenancy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.edmaputra.uwati.core.tenancy.domain.TenantId;

class ThreadLocalTenantContextTests {

	@Test
	void restoresThePreviousTenantWhenScopeCloses() {
		ThreadLocalTenantContext context = new ThreadLocalTenantContext();
		TenantId firstTenant = TenantId.generate();
		TenantId secondTenant = TenantId.generate();

		try (var firstScope = context.open(firstTenant)) {
			try (var secondScope = context.open(secondTenant)) {
				assertThat(context.requireTenantId()).isEqualTo(secondTenant);
			}
			assertThat(context.requireTenantId()).isEqualTo(firstTenant);
		}

		assertThat(context.currentTenantId()).isEmpty();
	}
}
