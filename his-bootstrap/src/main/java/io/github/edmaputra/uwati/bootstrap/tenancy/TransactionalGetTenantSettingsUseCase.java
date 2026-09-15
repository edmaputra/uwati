package io.github.edmaputra.uwati.bootstrap.tenancy;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.edmaputra.uwati.core.tenancy.application.service.GetTenantSettingsService;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.GetTenantSettingsUseCase;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantRepository;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantSettingRepository;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;

/**
 * Transactional decorator and Spring service bean wiring for {@link GetTenantSettingsUseCase}.
 * <p>
 * Wraps {@link GetTenantSettingsService} with read-only transaction demarcation.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@Service
public class TransactionalGetTenantSettingsUseCase implements GetTenantSettingsUseCase {

	private final GetTenantSettingsService delegate;

	/**
	 * Constructs the transactional get-settings use case with required repositories.
	 *
	 * @param tenantRepository the repository for accessing tenant records
	 * @param tenantSettingRepository the repository for retrieving tenant settings
	 */
	public TransactionalGetTenantSettingsUseCase(
			TenantRepository tenantRepository,
			TenantSettingRepository tenantSettingRepository) {
		this.delegate = new GetTenantSettingsService(tenantRepository, tenantSettingRepository);
	}

	/**
	 * Retrieves all settings for the given tenant within a read-only transaction.
	 *
	 * @param tenantId the unique identifier of the tenant
	 * @return list of tenant settings for the given tenant
	 * @throws NullPointerException if {@code tenantId} is null
	 * @throws io.github.edmaputra.uwati.domain.tenancy.domain.TenantNotFoundException if tenant does not exist
	 */
	@Override
	@Transactional(readOnly = true)
	public List<TenantSetting> execute(TenantId tenantId) {
		return delegate.execute(tenantId);
	}
}
