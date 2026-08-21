package io.github.edmaputra.uwati.domain.tenancy.application.port.out;

import java.util.Optional;

import io.github.edmaputra.uwati.domain.tenancy.domain.Tenant;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;

public interface TenantRepository {

	Optional<Tenant> findById(TenantId tenantId);

	Optional<Tenant> findByDisplayName(String displayName);

	Tenant save(Tenant tenant);
}
