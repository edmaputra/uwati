package io.github.edmaputra.uwati.domain.tenancy.application.port.in;

import java.util.List;

import io.github.edmaputra.iam.domain.context.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;

/**
 * Inbound port interface defining the use case for configuring or updating tenant settings.
 * <p>
 * Invoked by driving adapters to validate and persist configuration settings for a tenant
 * in the application layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public interface ConfigureTenantSettingsUseCase {

	/**
	 * Executes the configuration of tenant settings according to the provided command and operation context.
	 *
	 * @param command the command containing tenant ID and setting entries
	 * @param context the operation context with actor identity and tracing info
	 * @return the list of saved or updated {@link TenantSetting} instances
	 * @throws io.github.edmaputra.uwati.domain.tenancy.domain.TenantNotFoundException if the tenant does not exist
	 * @throws io.github.edmaputra.uwati.domain.tenancy.domain.InvalidTenantSettingException if any setting fails validation
	 */
	List<TenantSetting> execute(ConfigureTenantSettingsCommand command, OperationContext context);
}
