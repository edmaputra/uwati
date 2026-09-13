package io.github.edmaputra.uwati.domain.organization;

/**
 * Represents the operational lifecycle status of a healthcare facility.
 * <p>
 * Controls whether a facility can actively accept patients and deliver clinical services
 * in the core domain layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public enum FacilityStatus {
	ACTIVE,
	INACTIVE
}
