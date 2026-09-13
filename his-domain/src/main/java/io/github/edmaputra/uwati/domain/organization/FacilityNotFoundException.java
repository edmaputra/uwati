package io.github.edmaputra.uwati.domain.organization;

public class FacilityNotFoundException extends RuntimeException {

	public FacilityNotFoundException(FacilityId id) {
		super("Facility not found with ID: " + id);
	}

	public FacilityNotFoundException(String message) {
		super(message);
	}
}
