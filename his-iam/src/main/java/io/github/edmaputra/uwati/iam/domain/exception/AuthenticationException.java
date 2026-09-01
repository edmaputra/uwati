package io.github.edmaputra.uwati.iam.domain.exception;

/**
 * Thrown when credential authentication fails or an account is inactive.
 */
public class AuthenticationException extends RuntimeException {

	/**
	 * Constructs the exception with a detail message.
	 *
	 * @param message the detail message
	 */
	public AuthenticationException(String message) {
		super(message);
	}

	/**
	 * Constructs the exception with a message and cause.
	 *
	 * @param message the detail message
	 * @param cause   the root cause
	 */
	public AuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}
}
