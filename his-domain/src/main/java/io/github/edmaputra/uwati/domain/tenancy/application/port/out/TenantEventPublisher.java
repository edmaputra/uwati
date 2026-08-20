package io.github.edmaputra.uwati.domain.tenancy.application.port.out;

import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantCreated;

public interface TenantEventPublisher {

	void publish(TenantCreated event);
}
