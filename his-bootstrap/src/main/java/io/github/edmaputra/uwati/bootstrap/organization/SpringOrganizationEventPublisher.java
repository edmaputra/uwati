package io.github.edmaputra.uwati.bootstrap.organization;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import io.github.edmaputra.uwati.domain.organization.event.FacilityCreated;
import io.github.edmaputra.uwati.domain.organization.event.FacilityStatusChanged;
import io.github.edmaputra.uwati.domain.organization.event.FacilityUpdated;
import io.github.edmaputra.uwati.domain.organization.event.ServiceUnitCreated;
import io.github.edmaputra.uwati.domain.organization.event.ServiceUnitStatusChanged;
import io.github.edmaputra.uwati.domain.organization.event.ServiceUnitUpdated;
import io.github.edmaputra.uwati.domain.organization.port.out.OrganizationEventPublisher;
import lombok.RequiredArgsConstructor;

/**
 * Spring application event publisher adapter implementing {@link OrganizationEventPublisher}.
 * <p>
 * Bridges organization-related domain events (facilities, service units) onto the Spring {@link ApplicationEventPublisher} event bus.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class SpringOrganizationEventPublisher implements OrganizationEventPublisher {

	private final ApplicationEventPublisher publisher;

	/**
	 * Publishes a {@link FacilityCreated} domain event to the application context.
	 *
	 * @param event the facility created event
	 */
	@Override
	public void publish(FacilityCreated event) {
		publisher.publishEvent(event);
	}

	/**
	 * Publishes a {@link FacilityUpdated} domain event to the application context.
	 *
	 * @param event the facility updated event
	 */
	@Override
	public void publish(FacilityUpdated event) {
		publisher.publishEvent(event);
	}

	/**
	 * Publishes a {@link FacilityStatusChanged} domain event to the application context.
	 *
	 * @param event the facility status changed event
	 */
	@Override
	public void publish(FacilityStatusChanged event) {
		publisher.publishEvent(event);
	}

	/**
	 * Publishes a {@link ServiceUnitCreated} domain event to the application context.
	 *
	 * @param event the service unit created event
	 */
	@Override
	public void publish(ServiceUnitCreated event) {
		publisher.publishEvent(event);
	}

	/**
	 * Publishes a {@link ServiceUnitUpdated} domain event to the application context.
	 *
	 * @param event the service unit updated event
	 */
	@Override
	public void publish(ServiceUnitUpdated event) {
		publisher.publishEvent(event);
	}

	/**
	 * Publishes a {@link ServiceUnitStatusChanged} domain event to the application context.
	 *
	 * @param event the service unit status changed event
	 */
	@Override
	public void publish(ServiceUnitStatusChanged event) {
		publisher.publishEvent(event);
	}
}
