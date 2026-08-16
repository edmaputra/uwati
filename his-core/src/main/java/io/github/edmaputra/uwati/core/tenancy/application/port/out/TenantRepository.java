package io.github.edmaputra.uwati.core.tenancy.application.port.out;

import java.util.Optional;

import io.github.edmaputra.uwati.core.tenancy.domain.Tenant;
import io.github.edmaputra.uwati.core.tenancy.domain.TenantId;

public interface TenantRepository {

	Optional<Tenant> findById(TenantId tenantId);
}
