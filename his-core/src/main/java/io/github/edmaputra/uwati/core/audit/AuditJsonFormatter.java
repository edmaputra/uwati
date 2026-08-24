package io.github.edmaputra.uwati.core.audit;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import io.github.edmaputra.uwati.core.audit.AuditDiffEngine.CollectionDiff;
import io.github.edmaputra.uwati.core.audit.AuditDiffEngine.ElementDiff;
import io.github.edmaputra.uwati.core.audit.AuditDiffEngine.FieldDiff;

/**
 * Formats structured audit differences into clean, standard JSON strings
 * without redundant wrapper terms like "fields" or "collections".
 */
public final class AuditJsonFormatter {

	private AuditJsonFormatter() {
	}

	public static String formatDiff(Map<String, FieldDiff> fieldDiffs) {
		return formatDiff(fieldDiffs, Map.of());
	}

	public static <T> String formatCollectionDiff(
			String collectionName,
			CollectionDiff<T> collectionDiff,
			Function<T, String> elementJsonSerializer) {
		return formatDiff(Map.of(), Map.of(collectionName, new SerializedCollectionDiff<>(collectionDiff, elementJsonSerializer)));
	}

	public record SerializedCollectionDiff<T>(
			CollectionDiff<T> diff,
			Function<T, String> serializer) {
	}

	public static String formatDiff(
			Map<String, FieldDiff> fieldDiffs,
			Map<String, SerializedCollectionDiff<?>> collectionDiffs) {

		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean needsComma = false;

		if (fieldDiffs != null && !fieldDiffs.isEmpty()) {
			boolean firstField = true;
			for (Map.Entry<String, FieldDiff> entry : fieldDiffs.entrySet()) {
				if (!firstField) {
					sb.append(",");
				}
				firstField = false;
				sb.append("\"").append(escapeJson(entry.getKey())).append("\":{");
				sb.append("\"old\":").append(toJsonValue(entry.getValue().oldValue())).append(",");
				sb.append("\"new\":").append(toJsonValue(entry.getValue().newValue()));
				sb.append("}");
			}
			needsComma = true;
		}

		if (collectionDiffs != null && !collectionDiffs.isEmpty()) {
			for (Map.Entry<String, SerializedCollectionDiff<?>> entry : collectionDiffs.entrySet()) {
				if (needsComma) {
					sb.append(",");
				}
				needsComma = true;
				sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
				writeCollectionDiff(sb, entry.getValue());
			}
		}

		sb.append("}");
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	private static <T> void writeCollectionDiff(StringBuilder sb, SerializedCollectionDiff<T> serialized) {
		CollectionDiff<T> diff = serialized.diff();
		Function<T, String> serializer = serialized.serializer() != null
				? serialized.serializer()
				: item -> toJsonValue(item);

		sb.append("{");

		// "added"
		sb.append("\"added\":[");
		boolean firstAdded = true;
		for (T item : diff.added()) {
			if (!firstAdded) {
				sb.append(",");
			}
			firstAdded = false;
			sb.append(serializer.apply(item));
		}
		sb.append("],");

		// "removed"
		sb.append("\"removed\":[");
		boolean firstRemoved = true;
		for (T item : diff.removed()) {
			if (!firstRemoved) {
				sb.append(",");
			}
			firstRemoved = false;
			sb.append(serializer.apply(item));
		}
		sb.append("],");

		// "changed"
		sb.append("\"changed\":[");
		boolean firstChanged = true;
		for (ElementDiff change : diff.changed()) {
			if (!firstChanged) {
				sb.append(",");
			}
			firstChanged = false;
			sb.append("{\"key\":\"").append(escapeJson(change.key())).append("\"");
			for (Map.Entry<String, FieldDiff> fEntry : change.fields().entrySet()) {
				sb.append(",\"").append(escapeJson(fEntry.getKey())).append("\":{");
				sb.append("\"old\":").append(toJsonValue(fEntry.getValue().oldValue())).append(",");
				sb.append("\"new\":").append(toJsonValue(fEntry.getValue().newValue()));
				sb.append("}");
			}
			sb.append("}");
		}
		sb.append("]");

		sb.append("}");
	}

	public static String toJsonValue(Object value) {
		if (value == null) {
			return "null";
		}
		if (value instanceof Number || value instanceof Boolean) {
			return value.toString();
		}
		if (value instanceof CharSequence || value instanceof Enum<?> || value instanceof java.time.temporal.Temporal) {
			return "\"" + escapeJson(value.toString()) + "\"";
		}
		if (value instanceof Collection<?> col) {
			StringBuilder sb = new StringBuilder("[");
			boolean first = true;
			for (Object item : col) {
				if (!first) sb.append(",");
				first = false;
				sb.append(toJsonValue(item));
			}
			sb.append("]");
			return sb.toString();
		}
		return "\"" + escapeJson(value.toString()) + "\"";
	}

	public static String escapeJson(String value) {
		if (value == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
				case '"' -> sb.append("\\\"");
				case '\\' -> sb.append("\\\\");
				case '\b' -> sb.append("\\b");
				case '\f' -> sb.append("\\f");
				case '\n' -> sb.append("\\n");
				case '\r' -> sb.append("\\r");
				case '\t' -> sb.append("\\t");
				default -> {
					if (c < ' ') {
						sb.append(String.format("\\u%04x", (int) c));
					}
					else {
						sb.append(c);
					}
				}
			}
		}
		return sb.toString();
	}
}
