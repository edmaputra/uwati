package io.github.edmaputra.uwati.core.audit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.github.edmaputra.uwati.domain.audit.Auditable;

/**
 * Engine for computing state differences between previous and updated objects or collections.
 */
public final class AuditDiffEngine {

	private AuditDiffEngine() {
	}

	public record FieldDiff(Object oldValue, Object newValue) {
	}

	public record ElementDiff(String key, Map<String, FieldDiff> fields) {
		public ElementDiff {
			Objects.requireNonNull(key, "Element diff key must not be null.");
			fields = fields != null ? Collections.unmodifiableMap(new LinkedHashMap<>(fields)) : Map.of();
		}
	}

	public record CollectionDiff<T>(
			List<T> added,
			List<T> removed,
			List<ElementDiff> changed) {

		public CollectionDiff {
			added = added != null ? List.copyOf(added) : List.of();
			removed = removed != null ? List.copyOf(removed) : List.of();
			changed = changed != null ? List.copyOf(changed) : List.of();
		}

		public boolean hasChanges() {
			return !added.isEmpty() || !removed.isEmpty() || !changed.isEmpty();
		}
	}

	/**
	 * Computes differences between two {@link Auditable} models by comparing their declared auditable fields.
	 */
	public static <T extends Auditable> Map<String, FieldDiff> diff(T oldEntity, T newEntity) {
		Map<String, ?> oldFields = oldEntity != null ? oldEntity.auditableFields() : null;
		Map<String, ?> newFields = newEntity != null ? newEntity.auditableFields() : null;
		return diffFields(oldFields, newFields);
	}

	/**
	 * Computes differences between two maps of property fields with deterministic alphabetical key ordering.
	 */
	public static Map<String, FieldDiff> diffFields(Map<String, ?> oldFields, Map<String, ?> newFields) {
		Map<String, FieldDiff> diffs = new LinkedHashMap<>();
		Set<String> allKeys = new TreeSet<>();
		if (oldFields != null) {
			allKeys.addAll(oldFields.keySet());
		}
		if (newFields != null) {
			allKeys.addAll(newFields.keySet());
		}

		for (String key : allKeys) {
			Object oldVal = oldFields != null ? oldFields.get(key) : null;
			Object newVal = newFields != null ? newFields.get(key) : null;
			if (!Objects.equals(oldVal, newVal)) {
				diffs.put(key, new FieldDiff(oldVal, newVal));
			}
		}
		return Collections.unmodifiableMap(diffs);
	}

	/**
	 * Computes differences for a collection of identifiable {@link Auditable} elements.
	 */
	public static <T extends Auditable, K> CollectionDiff<T> diffKeyedCollection(
			Collection<T> oldElements,
			Collection<T> newElements,
			Function<T, K> keyExtractor) {
		return diffKeyedCollection(oldElements, newElements, keyExtractor, AuditDiffEngine::diff);
	}

	/**
	 * Computes differences for a collection of identifiable (keyed) elements with a custom element differ.
	 */
	public static <T, K> CollectionDiff<T> diffKeyedCollection(
			Collection<T> oldElements,
			Collection<T> newElements,
			Function<T, K> keyExtractor,
			BiFunction<T, T, Map<String, FieldDiff>> elementFieldDiffer) {

		Objects.requireNonNull(keyExtractor, "Key extractor must not be null.");

		Map<K, T> oldMap = new LinkedHashMap<>();
		if (oldElements != null) {
			for (T item : oldElements) {
				if (item != null) {
					oldMap.put(keyExtractor.apply(item), item);
				}
			}
		}

		Map<K, T> newMap = new LinkedHashMap<>();
		if (newElements != null) {
			for (T item : newElements) {
				if (item != null) {
					newMap.put(keyExtractor.apply(item), item);
				}
			}
		}

		List<T> added = new ArrayList<>();
		List<T> removed = new ArrayList<>();
		List<ElementDiff> changed = new ArrayList<>();

		// Check new elements for additions and modifications
		for (Map.Entry<K, T> entry : newMap.entrySet()) {
			K key = entry.getKey();
			T newItem = entry.getValue();
			T oldItem = oldMap.get(key);

			if (oldItem == null) {
				added.add(newItem);
			}
			else if (elementFieldDiffer != null) {
				Map<String, FieldDiff> fieldDiffs = elementFieldDiffer.apply(oldItem, newItem);
				if (fieldDiffs != null && !fieldDiffs.isEmpty()) {
					changed.add(new ElementDiff(String.valueOf(key), fieldDiffs));
				}
			}
			else if (!Objects.equals(oldItem, newItem)) {
				Map<String, FieldDiff> fallbackDiff = Map.of("value", new FieldDiff(oldItem, newItem));
				changed.add(new ElementDiff(String.valueOf(key), fallbackDiff));
			}
		}

		// Check for removed elements
		for (Map.Entry<K, T> entry : oldMap.entrySet()) {
			K key = entry.getKey();
			if (!newMap.containsKey(key)) {
				removed.add(entry.getValue());
			}
		}

		return new CollectionDiff<>(added, removed, changed);
	}

	/**
	 * Computes differences for simple primitive collections (lists of strings, enums, numbers).
	 */
	public static <T> CollectionDiff<T> diffPrimitiveCollection(Collection<T> oldElements, Collection<T> newElements) {
		Set<T> oldSet = oldElements != null ? new TreeSet<>(oldElements) : Set.of();
		Set<T> newSet = newElements != null ? new TreeSet<>(newElements) : Set.of();

		List<T> added = new ArrayList<>();
		for (T item : newSet) {
			if (!oldSet.contains(item)) {
				added.add(item);
			}
		}

		List<T> removed = new ArrayList<>();
		for (T item : oldSet) {
			if (!newSet.contains(item)) {
				removed.add(item);
			}
		}

		return new CollectionDiff<>(added, removed, List.of());
	}
}
