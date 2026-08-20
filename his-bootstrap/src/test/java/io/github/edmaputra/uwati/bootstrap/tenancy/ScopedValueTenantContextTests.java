package io.github.edmaputra.uwati.bootstrap.tenancy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;

class ScopedValueTenantContextTests {

	@Test
	void restoresThePreviousTenantAfterNestedScopeCompletes() throws Throwable {
		ScopedValueTenantContext context = new ScopedValueTenantContext();
		TenantId firstTenant = TenantId.generate();
		TenantId secondTenant = TenantId.generate();

		context.callWithTenant(firstTenant, () -> {
			context.callWithTenant(secondTenant, () -> {
				assertThat(context.requireTenantId()).isEqualTo(secondTenant);
				return null;
			});
			assertThat(context.requireTenantId()).isEqualTo(firstTenant);
			return null;
		});

		assertThat(context.currentTenantId()).isEmpty();
	}
}
