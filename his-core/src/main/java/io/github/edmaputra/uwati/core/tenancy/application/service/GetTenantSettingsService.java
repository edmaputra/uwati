package io.github.edmaputra.uwati.core.tenancy.application.service;

import java.util.List;
import java.util.Objects;

import io.github.edmaputra.uwati.domain.tenancy.application.port.in.GetTenantSettingsUseCase;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantRepository;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantSettingRepository;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantNotFoundException;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing {@link GetTenantSettingsUseCase}.
 * <p>
 * Retrieves all configuration settings associated with a specific tenant after validating tenant existence.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@RequiredArgsConstructor
public class GetTenantSettingsService implements GetTenantSettingsUseCase {

	private final TenantRepository tenantRepository;
	private final TenantSettingRepository tenantSettingRepository;

	/**
	 * Retrieves all configuration settings for the specified tenant.
	 *
	 * @param tenantId the unique identifier of the tenant
	 * @return list of tenant settings for the given tenant
	 * @throws NullPointerException if {@code tenantId} is null
	 * @throws TenantNotFoundException if no tenant exists with the given ID
	 */
	@Override
	public List<TenantSetting> execute(TenantId tenantId) {
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");

		tenantRepository.findById(tenantId)
				.orElseThrow(() -> new TenantNotFoundException(tenantId));

		return tenantSettingRepository.findAllByTenantId(tenantId);
	}
}
