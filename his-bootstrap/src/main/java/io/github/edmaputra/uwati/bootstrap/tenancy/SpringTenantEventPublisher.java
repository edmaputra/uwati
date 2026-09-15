package io.github.edmaputra.uwati.bootstrap.tenancy;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantEventPublisher;
import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantCreated;
import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantSettingsUpdated;
import lombok.RequiredArgsConstructor;

/**
 * Spring application event publisher adapter implementing {@link TenantEventPublisher}.
 * <p>
 * Bridges domain tenancy events onto the Spring {@link ApplicationEventPublisher} event bus.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class SpringTenantEventPublisher implements TenantEventPublisher {

	private final ApplicationEventPublisher publisher;

	/**
	 * Publishes a {@link TenantCreated} domain event to the Spring application context.
	 *
	 * @param event the tenant created domain event
	 */
	@Override
	public void publish(TenantCreated event) {
		publisher.publishEvent(event);
	}

	/**
	 * Publishes a {@link TenantSettingsUpdated} domain event to the Spring application context.
	 *
	 * @param event the tenant settings updated domain event
	 */
	@Override
	public void publish(TenantSettingsUpdated event) {
		publisher.publishEvent(event);
	}
}
