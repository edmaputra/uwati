package io.github.edmaputra.uwati.iam.domain.exception;

import io.github.edmaputra.uwati.iam.domain.model.RoleId;

public class RoleNotFoundException extends RuntimeException {

	public RoleNotFoundException(RoleId roleId) {
		super("Role not found with id: " + roleId);
	}

	public RoleNotFoundException(String code) {
		super("Role not found with code: " + code);
	}
}
