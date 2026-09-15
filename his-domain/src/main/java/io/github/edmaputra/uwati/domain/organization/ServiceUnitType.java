package io.github.edmaputra.uwati.domain.organization;

/**
 * Represents the functional department or clinical unit type within a healthcare facility.
 * <p>
 * Distinguishes between clinical, diagnostic, surgical, and administrative service units
 * in the core domain layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public enum ServiceUnitType {
	OUTPATIENT_CLINIC,
	INPATIENT_WARD,
	EMERGENCY,
	INTENSIVE_CARE,
	PHARMACY,
	LABORATORY,
	RADIOLOGY,
	SURGERY_THEATER,
	CASHIER,
	ADMINISTRATIVE
}
