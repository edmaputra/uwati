package io.github.edmaputra.uwati.domain.organization;

public class ServiceUnitNotFoundException extends RuntimeException {

	public ServiceUnitNotFoundException(ServiceUnitId id) {
		super("Service unit not found with ID: " + id);
	}

	public ServiceUnitNotFoundException(String message) {
		super(message);
	}
}
