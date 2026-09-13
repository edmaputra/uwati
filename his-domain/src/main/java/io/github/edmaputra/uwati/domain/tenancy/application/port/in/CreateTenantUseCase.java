package io.github.edmaputra.uwati.domain.tenancy.application.port.in;

import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.Tenant;

/**
 * Inbound port interface defining the use case for creating a new tenant.
 * <p>
 * Invoked by driving adapters to orchestrate tenant provisioning, identity assignment,
 * and registration events in the application layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public interface CreateTenantUseCase {

	/**
	 * Executes the creation of a tenant with the given command and operation context.
	 *
	 * @param command the command containing legal and display names of the tenant
	 * @param context the operation context with actor identity and tracing info
	 * @return the newly created {@link Tenant} entity
	 * @throws io.github.edmaputra.uwati.domain.tenancy.domain.DuplicateTenantDisplayNameException if the display name already exists
	 */
	Tenant execute(CreateTenantCommand command, OperationContext context);
}
