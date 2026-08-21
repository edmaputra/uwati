package io.github.edmaputra.uwati.domain.tenancy.domain;

public class InvalidTenantSettingException extends IllegalArgumentException {

	public InvalidTenantSettingException(String message) {
		super(message);
	}
}
