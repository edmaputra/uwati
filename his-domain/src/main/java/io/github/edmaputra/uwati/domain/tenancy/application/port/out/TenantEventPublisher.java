package io.github.edmaputra.uwati.domain.tenancy.application.port.out;

import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantCreated;
import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantSettingsUpdated;

/**
 * Outbound port (SPI) interface for publishing tenant lifecycle and configuration domain events.
 * <p>
 * Implemented by driven messaging adapters (e.g. Spring ApplicationEventPublisher, Kafka, or RabbitMQ)
 * in the outbound port layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public interface TenantEventPublisher {

	/**
	 * Publishes a {@link TenantCreated} domain event to external subscribers.
	 *
	 * @param event the event payload containing the newly created tenant
	 */
	void publish(TenantCreated event);

	/**
	 * Publishes a {@link TenantSettingsUpdated} domain event to external subscribers.
	 *
	 * @param event the event payload containing the before-and-after setting snapshots
	 */
	void publish(TenantSettingsUpdated event);
}
