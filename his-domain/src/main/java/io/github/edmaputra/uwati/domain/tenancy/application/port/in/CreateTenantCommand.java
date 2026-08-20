package io.github.edmaputra.uwati.domain.tenancy.application.port.in;

public record CreateTenantCommand(String legalName, String displayName) {

	public CreateTenantCommand {
		if (legalName == null) {
			throw new IllegalArgumentException("Tenant legal name must not be null.");
		}
		if (displayName == null) {
			throw new IllegalArgumentException("Tenant display name must not be null.");
		}
		if (legalName.isBlank()) {
			throw new IllegalArgumentException("Tenant legal name must not be blank.");
		}
		if (displayName.isBlank()) {
			throw new IllegalArgumentException("Tenant display name must not be blank.");
		}
	}
}
