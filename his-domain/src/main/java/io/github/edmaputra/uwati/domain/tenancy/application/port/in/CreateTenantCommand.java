package io.github.edmaputra.uwati.domain.tenancy.application.port.in;

/**
 * Inbound command encapsulating the request to register a new tenant.
 * <p>
 * Passed across the inbound port boundary to {@link CreateTenantUseCase}
 * in the application layer of the Hexagonal Architecture.
 *
 * @param legalName the legal registered organization name of the tenant
 * @param displayName the human-friendly display name of the tenant
 * @author edmaputra
 * @since 0.0.1
 */
public record CreateTenantCommand(String legalName, String displayName) {

	/**
	 * Compact constructor validating that the legal name and display name are present and non-blank.
	 *
	 * @param legalName the legal organization name
	 * @param displayName the display name
	 * @throws IllegalArgumentException if {@code legalName} or {@code displayName} is null or blank
	 */
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
