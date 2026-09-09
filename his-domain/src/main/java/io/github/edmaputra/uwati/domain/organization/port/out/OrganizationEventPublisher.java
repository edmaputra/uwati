package io.github.edmaputra.uwati.domain.organization.port.out;

import io.github.edmaputra.uwati.domain.organization.event.FacilityCreated;
import io.github.edmaputra.uwati.domain.organization.event.FacilityStatusChanged;
import io.github.edmaputra.uwati.domain.organization.event.FacilityUpdated;
import io.github.edmaputra.uwati.domain.organization.event.ServiceUnitCreated;
import io.github.edmaputra.uwati.domain.organization.event.ServiceUnitStatusChanged;
import io.github.edmaputra.uwati.domain.organization.event.ServiceUnitUpdated;

public interface OrganizationEventPublisher {

	void publish(FacilityCreated event);

	void publish(FacilityUpdated event);

	void publish(FacilityStatusChanged event);

	void publish(ServiceUnitCreated event);

	void publish(ServiceUnitUpdated event);

	void publish(ServiceUnitStatusChanged event);
}
