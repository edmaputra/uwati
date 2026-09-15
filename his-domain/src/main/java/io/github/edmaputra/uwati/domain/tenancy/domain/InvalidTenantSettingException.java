package io.github.edmaputra.uwati.domain.tenancy.domain;

/**
 * Domain exception thrown when a tenant setting key is unsupported or its value fails validation rules.
 * <p>
 * Signals configuration format or constraint violations within the core domain layer of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public class InvalidTenantSettingException extends IllegalArgumentException {

	/**
	 * Constructs a new {@code InvalidTenantSettingException} with the specified detail message.
	 *
	 * @param message the detail explanation of the validation failure
	 */
	public InvalidTenantSettingException(String message) {
		super(message);
	}
}
