package io.github.edmaputra.uwati.iam.domain.exception;

public class AccessDeniedException extends RuntimeException {

	public AccessDeniedException(String message) {
		super(message);
	}
}
