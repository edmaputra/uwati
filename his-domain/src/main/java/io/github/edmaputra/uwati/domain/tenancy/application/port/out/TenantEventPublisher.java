package io.github.edmaputra.uwati.domain.tenancy.application.port.out;

import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantCreated;
import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantSettingsUpdated;

public interface TenantEventPublisher {

	void publish(TenantCreated event);

	void publish(TenantSettingsUpdated event);
}
