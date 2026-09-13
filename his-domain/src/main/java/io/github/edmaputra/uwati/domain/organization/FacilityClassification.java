package io.github.edmaputra.uwati.domain.organization;

/**
 * Represents the official classification level of a healthcare facility.
 * <p>
 * Models standard healthcare facility classifications (e.g. hospital classes A-D and clinic levels Pratama/Utama)
 * in the core domain layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public enum FacilityClassification {
	CLASS_A,
	CLASS_B,
	CLASS_C,
	CLASS_D,
	PRATAMA,
	UTAMA,
	NONE
}
