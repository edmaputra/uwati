package io.github.edmaputra.uwati.core.tenancy.application.port.out;

import io.github.edmaputra.uwati.core.tenancy.domain.event.TenantCreated;

public interface TenantEventPublisher {

	void publish(TenantCreated event);
}
