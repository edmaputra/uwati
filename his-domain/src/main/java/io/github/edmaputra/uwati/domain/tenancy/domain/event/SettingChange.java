package io.github.edmaputra.uwati.domain.tenancy.domain.event;

import java.util.Objects;

/**
 * Represents a single setting change within a tenant settings update event.
 * Captures the change type (ADDED, CHANGED) along with old and new values.
 */
public record SettingChange(
		String key,
		ChangeType changeType,
		String oldValue,
		String newValue,
		int oldRevision,
		int newRevision) {

	public enum ChangeType {
		ADDED, CHANGED
	}

	public SettingChange {
		Objects.requireNonNull(key, "Key must not be null.");
		Objects.requireNonNull(changeType, "Change type must not be null.");
		Objects.requireNonNull(newValue, "New value must not be null.");
	}

	public static SettingChange added(String key, String newValue, int newRevision) {
		return new SettingChange(key, ChangeType.ADDED, null, newValue, 0, newRevision);
	}

	public static SettingChange changed(String key, String oldValue, String newValue, int oldRevision, int newRevision) {
		return new SettingChange(key, ChangeType.CHANGED, oldValue, newValue, oldRevision, newRevision);
	}
}
