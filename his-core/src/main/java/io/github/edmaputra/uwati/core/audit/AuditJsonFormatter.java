package io.github.edmaputra.uwati.core.audit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import io.github.edmaputra.uwati.core.audit.AuditDiffEngine.CollectionDiff;
import io.github.edmaputra.uwati.core.audit.AuditDiffEngine.ElementDiff;
import io.github.edmaputra.uwati.core.audit.AuditDiffEngine.FieldDiff;

/**
 * Formats structured audit differences into clean, standard JSON strings using Jackson ObjectMapper.
 */
public final class AuditJsonFormatter {

	private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();

	private AuditJsonFormatter() {
	}

	public static String formatDiff(Map<String, FieldDiff> fieldDiffs) {
		return formatDiff(fieldDiffs, null, null, null);
	}

	public static <T> String formatCollectionDiff(
			String collectionName,
			CollectionDiff<T> collectionDiff) {
		return formatDiff(Map.of(), collectionName, collectionDiff, null);
	}

	public static <T> String formatCollectionDiff(
			String collectionName,
			CollectionDiff<T> collectionDiff,
			Function<T, ?> elementSerializer) {
		return formatDiff(Map.of(), collectionName, collectionDiff, elementSerializer);
	}

	public static <T> String formatDiff(
			Map<String, FieldDiff> fieldDiffs,
			String collectionName,
			CollectionDiff<T> collectionDiff,
			Function<T, ?> elementSerializer) {

		Map<String, Object> root = new LinkedHashMap<>();

		// 1. Entity field diffs directly at root
		if (fieldDiffs != null && !fieldDiffs.isEmpty()) {
			for (Map.Entry<String, FieldDiff> entry : fieldDiffs.entrySet()) {
				Map<String, Object> valDiff = new LinkedHashMap<>();
				valDiff.put("old", entry.getValue().oldValue());
				valDiff.put("new", entry.getValue().newValue());
				root.put(entry.getKey(), valDiff);
			}
		}

		// 2. Collection diff under its collection name
		if (collectionName != null && collectionDiff != null) {
			Map<String, Object> colMap = new LinkedHashMap<>();

			Function<T, Object> serializer = elementSerializer != null
					? item -> (Object) elementSerializer.apply(item)
					: item -> (Object) item;

			List<Object> added = new ArrayList<>();
			for (T item : collectionDiff.added()) {
				added.add(serializer.apply(item));
			}
			colMap.put("added", added);

			List<Object> removed = new ArrayList<>();
			for (T item : collectionDiff.removed()) {
				removed.add(serializer.apply(item));
			}
			colMap.put("removed", removed);

			List<Map<String, Object>> changed = new ArrayList<>();
			for (ElementDiff change : collectionDiff.changed()) {
				Map<String, Object> changeMap = new LinkedHashMap<>();
				changeMap.put("key", change.key());
				for (Map.Entry<String, FieldDiff> fEntry : change.fields().entrySet()) {
					Map<String, Object> fValDiff = new LinkedHashMap<>();
					fValDiff.put("old", fEntry.getValue().oldValue());
					fValDiff.put("new", fEntry.getValue().newValue());
					changeMap.put(fEntry.getKey(), fValDiff);
				}
				changed.add(changeMap);
			}
			colMap.put("changed", changed);

			root.put(collectionName, colMap);
		}

		try {
			return OBJECT_MAPPER.writeValueAsString(root);
		}
		catch (Exception e) {
			throw new IllegalStateException("Failed to serialize audit diff to JSON", e);
		}
	}
}
