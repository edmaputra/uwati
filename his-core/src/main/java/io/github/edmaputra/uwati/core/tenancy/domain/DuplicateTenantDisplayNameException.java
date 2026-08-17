package io.github.edmaputra.uwati.core.tenancy.domain;

public class DuplicateTenantDisplayNameException extends IllegalArgumentException {

	public DuplicateTenantDisplayNameException(String displayName) {
		super("A tenant with the display name '%s' already exists.".formatted(displayName));
	}
}
