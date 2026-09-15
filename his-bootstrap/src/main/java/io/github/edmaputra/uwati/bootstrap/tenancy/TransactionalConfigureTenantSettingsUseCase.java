package io.github.edmaputra.uwati.bootstrap.tenancy;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.edmaputra.uwati.core.tenancy.application.service.ConfigureTenantSettingsService;
import io.github.edmaputra.iam.domain.context.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.ConfigureTenantSettingsCommand;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.ConfigureTenantSettingsUseCase;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantEventPublisher;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantRepository;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantSettingRepository;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;

/**
 * Transactional decorator and Spring service bean wiring for {@link ConfigureTenantSettingsUseCase}.
 * <p>
 * Wraps {@link ConfigureTenantSettingsService} with Spring declarative transaction demarcation.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@Service
public class TransactionalConfigureTenantSettingsUseCase implements ConfigureTenantSettingsUseCase {

	private final ConfigureTenantSettingsService delegate;

	/**
	 * Constructs the transactional use case wrapper with required dependencies.
	 *
	 * @param tenantRepository the repository for accessing tenant data
	 * @param tenantSettingRepository the repository for accessing and storing tenant settings
	 * @param eventPublisher the publisher for tenancy domain events
	 */
	public TransactionalConfigureTenantSettingsUseCase(
			TenantRepository tenantRepository,
			TenantSettingRepository tenantSettingRepository,
			TenantEventPublisher eventPublisher) {
		this.delegate = new ConfigureTenantSettingsService(tenantRepository, tenantSettingRepository, eventPublisher);
	}

	/**
	 * Configures or updates tenant settings within a transactional boundary.
	 *
	 * @param command the configuration command containing tenant ID and setting entries
	 * @param context the operation context with actor and correlation details
	 * @return list of saved or updated tenant settings
	 * @throws NullPointerException if {@code command} or {@code context} is null
	 * @throws io.github.edmaputra.uwati.domain.tenancy.domain.TenantNotFoundException if target tenant does not exist
	 * @throws io.github.edmaputra.uwati.domain.tenancy.domain.InvalidTenantSettingException if any setting key is unsupported or value format is invalid
	 */
	@Override
	@Transactional
	public List<TenantSetting> execute(ConfigureTenantSettingsCommand command, OperationContext context) {
		return delegate.execute(command, context);
	}
}

