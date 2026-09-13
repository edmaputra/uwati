package io.github.edmaputra.uwati.domain.organization;

public class DuplicateFacilityCodeException extends RuntimeException {

	public DuplicateFacilityCodeException(String code) {
		super("Facility code '" + code + "' already exists for this tenant.");
	}
}
