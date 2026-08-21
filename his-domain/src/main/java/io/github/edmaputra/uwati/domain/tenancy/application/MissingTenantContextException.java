package io.github.edmaputra.uwati.domain.tenancy.application;

public class MissingTenantContextException extends IllegalStateException {

	public MissingTenantContextException(String message) {
		super(message);
	}
}
