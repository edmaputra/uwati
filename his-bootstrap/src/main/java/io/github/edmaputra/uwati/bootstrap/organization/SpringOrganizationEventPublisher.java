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

@Component
@RequiredArgsConstructor
public class SpringOrganizationEventPublisher implements OrganizationEventPublisher {

	private final ApplicationEventPublisher publisher;

	@Override
	public void publish(FacilityCreated event) {
		publisher.publishEvent(event);
	}

	@Override
	public void publish(FacilityUpdated event) {
		publisher.publishEvent(event);
	}

	@Override
	public void publish(FacilityStatusChanged event) {
		publisher.publishEvent(event);
	}

	@Override
	public void publish(ServiceUnitCreated event) {
		publisher.publishEvent(event);
	}

	@Override
	public void publish(ServiceUnitUpdated event) {
		publisher.publishEvent(event);
	}

	@Override
	public void publish(ServiceUnitStatusChanged event) {
		publisher.publishEvent(event);
	}
}
