package io.github.edmaputra.uwati.domain.tenancy.application.port.in;

import java.util.List;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;

/**
 * Inbound port interface defining the use case for querying all settings belonging to a tenant.
 * <p>
 * Invoked by driving adapters to retrieve current tenant configuration
 * in the application layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public interface GetTenantSettingsUseCase {

	/**
	 * Retrieves all configuration settings for the specified tenant identifier.
	 *
	 * @param tenantId the identifier of the tenant
	 * @return the list of {@link TenantSetting} instances configured for the tenant, or empty if none
	 */
	List<TenantSetting> execute(TenantId tenantId);
}
