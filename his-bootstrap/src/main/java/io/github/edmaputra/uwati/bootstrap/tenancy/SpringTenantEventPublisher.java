package io.github.edmaputra.uwati.bootstrap.tenancy;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantEventPublisher;
import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantCreated;
import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantSettingsUpdated;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SpringTenantEventPublisher implements TenantEventPublisher {

	private final ApplicationEventPublisher publisher;

	@Override
	public void publish(TenantCreated event) {
		publisher.publishEvent(event);
	}

	@Override
	public void publish(TenantSettingsUpdated event) {
		publisher.publishEvent(event);
	}
}
