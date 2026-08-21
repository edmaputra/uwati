package io.github.edmaputra.uwati.domain.tenancy.domain;

public class TenantNotFoundException extends IllegalArgumentException {

	public TenantNotFoundException(TenantId tenantId) {
		super("Tenant with ID '%s' was not found.".formatted(tenantId));
	}
}
