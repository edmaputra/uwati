package io.github.edmaputra.uwati.domain.organization.port.out;

import io.github.edmaputra.uwati.domain.organization.event.FacilityCreated;
import io.github.edmaputra.uwati.domain.organization.event.FacilityStatusChanged;
import io.github.edmaputra.uwati.domain.organization.event.FacilityUpdated;
import io.github.edmaputra.uwati.domain.organization.event.ServiceUnitCreated;
import io.github.edmaputra.uwati.domain.organization.event.ServiceUnitStatusChanged;
import io.github.edmaputra.uwati.domain.organization.event.ServiceUnitUpdated;

/**
 * Outbound port (SPI) interface for publishing organization domain events.
 * <p>
 * Implemented by driven messaging adapters (e.g. Spring ApplicationEventPublisher or message brokers)
 * in the outbound port layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public interface OrganizationEventPublisher {

	/**
	 * Publishes a {@link FacilityCreated} domain event to external subscribers.
	 *
	 * @param event the event payload containing the newly created facility
	 */
	void publish(FacilityCreated event);

	/**
	 * Publishes a {@link FacilityUpdated} domain event to external subscribers.
	 *
	 * @param event the event payload containing previous and updated facility states
	 */
	void publish(FacilityUpdated event);

	/**
	 * Publishes a {@link FacilityStatusChanged} domain event to external subscribers.
	 *
	 * @param event the event payload containing previous and updated facility statuses
	 */
	void publish(FacilityStatusChanged event);

	/**
	 * Publishes a {@link ServiceUnitCreated} domain event to external subscribers.
	 *
	 * @param event the event payload containing the newly created service unit
	 */
	void publish(ServiceUnitCreated event);

	/**
	 * Publishes a {@link ServiceUnitUpdated} domain event to external subscribers.
	 *
	 * @param event the event payload containing previous and updated service unit states
	 */
	void publish(ServiceUnitUpdated event);

	/**
	 * Publishes a {@link ServiceUnitStatusChanged} domain event to external subscribers.
	 *
	 * @param event the event payload containing previous and updated service unit statuses
	 */
	void publish(ServiceUnitStatusChanged event);
}
