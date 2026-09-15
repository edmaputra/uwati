package io.github.edmaputra.uwati.domain.organization;

/**
 * Represents the operational lifecycle status of a service unit.
 * <p>
 * Dictates whether a department or clinic unit can currently schedule visits and handle patient orders
 * in the core domain layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public enum ServiceUnitStatus {
	ACTIVE,
	INACTIVE
}
