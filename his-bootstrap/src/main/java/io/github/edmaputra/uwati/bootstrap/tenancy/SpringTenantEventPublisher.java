package io.github.edmaputra.uwati.bootstrap.tenancy;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import io.github.edmaputra.uwati.core.tenancy.application.port.out.TenantEventPublisher;
import io.github.edmaputra.uwati.core.tenancy.domain.event.TenantCreated;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SpringTenantEventPublisher implements TenantEventPublisher {

	private final ApplicationEventPublisher publisher;

	@Override
	public void publish(TenantCreated event) {
		publisher.publishEvent(event);
	}
}
