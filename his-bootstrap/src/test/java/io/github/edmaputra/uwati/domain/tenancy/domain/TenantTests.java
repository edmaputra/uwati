package io.github.edmaputra.uwati.domain.tenancy.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class TenantTests {

	private static final Instant CREATED_AT = Instant.parse("2026-08-16T10:00:00Z");
	private static final TenantId TENANT_ID = TenantId.generate();

	@Test
	void retainsTheTenantRegistryFields() {
		Tenant tenant = new Tenant(
				TENANT_ID,
				"Uwati Health Services Ltd.",
				"Uwati Health",
				TenantStatus.ACTIVE,
				CREATED_AT,
				CREATED_AT.plusSeconds(60));

		assertThat(tenant.id()).isEqualTo(TENANT_ID);
		assertThat(tenant.legalName()).isEqualTo("Uwati Health Services Ltd.");
		assertThat(tenant.displayName()).isEqualTo("Uwati Health");
		assertThat(tenant.status()).isEqualTo(TenantStatus.ACTIVE);
		assertThat(tenant.createdAt()).isEqualTo(CREATED_AT);
		assertThat(tenant.updatedAt()).isEqualTo(CREATED_AT.plusSeconds(60));
		assertThat(tenant.isActive()).isTrue();
	}

	@Test
	void rejectsAnUpdateTimestampBeforeCreation() {
		assertThatIllegalArgumentException().isThrownBy(() -> new Tenant(
				TENANT_ID,
				"Uwati Health Services Ltd.",
				"Uwati Health",
				TenantStatus.ACTIVE,
				CREATED_AT,
				CREATED_AT.minusSeconds(1)))
				.withMessage("Tenant update timestamp must not precede its creation timestamp.");
	}

	@Test
	void rejectsMissingOrBlankRequiredFields() {
		assertThatNullPointerException().isThrownBy(() -> new Tenant(
				null,
				"Uwati Health Services Ltd.",
				"Uwati Health",
				TenantStatus.ACTIVE,
				CREATED_AT,
				CREATED_AT));
		assertThatIllegalArgumentException().isThrownBy(() -> new Tenant(
				TENANT_ID,
				" ",
				"Uwati Health",
				TenantStatus.ACTIVE,
				CREATED_AT,
				CREATED_AT));
		assertThatIllegalArgumentException().isThrownBy(() -> new Tenant(
				TENANT_ID,
				"Uwati Health Services Ltd.",
				" ",
				TenantStatus.ACTIVE,
				CREATED_AT,
				CREATED_AT));
	}
}
