package io.github.edmaputra.uwati.iam.domain.exception;

import io.github.edmaputra.uwati.iam.domain.model.UserId;

public class UserNotFoundException extends RuntimeException {

	public UserNotFoundException(UserId userId) {
		super("User not found with id: " + userId);
	}

	public UserNotFoundException(String email) {
		super("User not found with email: " + email);
	}
}
