package io.github.edmaputra.uwati.domain.organization;

public class DuplicateServiceUnitCodeException extends RuntimeException {

	public DuplicateServiceUnitCodeException(String code) {
		super("Service unit code '" + code + "' already exists for this facility.");
	}
}
