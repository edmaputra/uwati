package io.github.edmaputra.uwati.core.tenancy.application;

public class MissingTenantContextException extends IllegalStateException {

	public MissingTenantContextException(String message) {
		super(message);
	}
}
