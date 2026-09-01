package io.github.edmaputra.uwati.domain.tenancy.domain;

import java.time.ZoneId;
import java.util.Currency;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Domain validator ensuring tenant configuration settings conform to format, ISO standards, and supported keys.
 *
 * @author edmaputra
 */
public final class TenantSettingValidator {

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

	private static final Set<String> ALLOWED_MEASUREMENT_SYSTEMS = Set.of("METRIC", "IMPERIAL");

	private static final Set<String> SUPPORTED_KEYS = Set.of(
			"organization.locale",
			"organization.time-zone",
			"organization.tax-id",
			"organization.contact-email",
			"organization.contact-phone",
			"finance.currency",
			"inventory.measurement-system",
			"features.base-configuration");

	private TenantSettingValidator() {
	}

	/**
	 * Validates a tenant setting key-value pair against domain format rules.
	 *
	 * @param key the setting key
	 * @param value the setting value
	 * @throws InvalidTenantSettingException if the key is unsupported or value format is invalid
	 */
	public static void validate(String key, String value) {
		if (key == null || key.isBlank()) {
			throw new InvalidTenantSettingException("Setting key must not be blank.");
		}
		if (value == null || value.isBlank()) {
			throw new InvalidTenantSettingException("Setting value for key '%s' must not be blank.".formatted(key));
		}
		if (!SUPPORTED_KEYS.contains(key)) {
			throw new InvalidTenantSettingException("Unsupported setting key '%s'.".formatted(key));
		}

		switch (key) {
			case "organization.locale" -> validateLocale(key, value);
			case "organization.time-zone" -> validateTimeZone(key, value);
			case "finance.currency" -> validateCurrency(key, value);
			case "inventory.measurement-system" -> validateMeasurementSystem(key, value);
			case "organization.contact-email" -> validateEmail(key, value);
			default -> {
				// Non-blank validation already passed above
			}
		}
	}

	private static void validateLocale(String key, String value) {
		try {
			Locale locale = new Locale.Builder().setLanguageTag(value).build();
			if (locale.getLanguage().isBlank() || !Set.of(Locale.getISOLanguages()).contains(locale.getLanguage())) {
				throw new InvalidTenantSettingException("Invalid locale '%s' for setting '%s'.".formatted(value, key));
			}
		}
		catch (Exception exception) {
			throw new InvalidTenantSettingException("Invalid locale '%s' for setting '%s'.".formatted(value, key));
		}
	}

	private static void validateTimeZone(String key, String value) {
		try {
			ZoneId.of(value);
		}
		catch (Exception exception) {
			throw new InvalidTenantSettingException("Invalid timezone '%s' for setting '%s'.".formatted(value, key));
		}
	}

	private static void validateCurrency(String key, String value) {
		try {
			Currency.getInstance(value);
		}
		catch (Exception exception) {
			throw new InvalidTenantSettingException("Invalid currency code '%s' for setting '%s'.".formatted(value, key));
		}
	}

	private static void validateMeasurementSystem(String key, String value) {
		if (!ALLOWED_MEASUREMENT_SYSTEMS.contains(value.toUpperCase(Locale.ROOT))) {
			throw new InvalidTenantSettingException(
					"Invalid measurement system '%s' for setting '%s'. Must be one of %s.".formatted(
							value, key, ALLOWED_MEASUREMENT_SYSTEMS));
		}
	}

	private static void validateEmail(String key, String value) {
		if (!EMAIL_PATTERN.matcher(value).matches()) {
			throw new InvalidTenantSettingException("Invalid email address '%s' for setting '%s'.".formatted(value, key));
		}
	}
}
