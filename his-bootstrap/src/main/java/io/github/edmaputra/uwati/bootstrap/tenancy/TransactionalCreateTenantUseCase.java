package io.github.edmaputra.uwati.bootstrap.tenancy;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.edmaputra.uwati.core.tenancy.application.service.CreateTenantService;
import io.github.edmaputra.iam.domain.context.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.CreateTenantCommand;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.CreateTenantUseCase;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantEventPublisher;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantRepository;
import io.github.edmaputra.uwati.domain.tenancy.domain.Tenant;

/**
 * Transactional decorator and Spring service bean wiring for {@link CreateTenantUseCase}.
 * <p>
 * Wraps {@link CreateTenantService} with Spring declarative transaction demarcation.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@Service
public class TransactionalCreateTenantUseCase implements CreateTenantUseCase {

	private final CreateTenantService delegate;

	/**
	 * Constructs the transactional create-tenant use case with required repository and publisher.
	 *
	 * @param tenantRepository the repository for persisting tenant entities
	 * @param eventPublisher the publisher for tenancy domain events
	 */
	public TransactionalCreateTenantUseCase(TenantRepository tenantRepository, TenantEventPublisher eventPublisher) {
		this.delegate = new CreateTenantService(tenantRepository, eventPublisher);
	}

	/**
	 * Creates a new tenant or retrieves an existing matching tenant within a transaction.
	 *
	 * @param command the tenant creation command containing legal and display names
	 * @param context the operation context with actor and correlation details
	 * @return the created or matching tenant entity
	 * @throws NullPointerException if {@code command} or {@code context} is null
	 * @throws IllegalArgumentException if legal name or display name is blank
	 * @throws io.github.edmaputra.uwati.domain.tenancy.domain.DuplicateTenantDisplayNameException if display name already exists for another entity
	 */
	@Override
	@Transactional
	public Tenant execute(CreateTenantCommand command, OperationContext context) {
		return delegate.execute(command, context);
	}
}

