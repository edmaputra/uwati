package io.github.edmaputra.uwati.core.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.edmaputra.uwati.core.audit.AuditDiffEngine.CollectionDiff;
import io.github.edmaputra.uwati.core.audit.AuditDiffEngine.FieldDiff;
import io.github.edmaputra.uwati.domain.audit.Auditable;

@DisplayName("AuditDiffEngine & AuditJsonFormatter Unit Tests")
class AuditDiffEngineTests {

	record Setting(String key, String value, int revision, String internalMetadata) implements Auditable {
		@Override
		public Map<String, Object> auditableFields() {
			// internalMetadata is deliberately omitted from audit trail
			return Map.of(
					"value", value,
					"revision", revision);
		}
	}

	record SimpleEntity(String name, String status, String unmonitoredField) implements Auditable {
		@Override
		public Map<String, Object> auditableFields() {
			return Map.of(
					"name", name,
					"status", status);
		}
	}

	@Test
	@DisplayName("computes differences between Auditable models only on declared fields")
	void computesAuditableModelDiffs() {
		SimpleEntity oldEntity = new SimpleEntity("Old Name", "ACTIVE", "secret1");
		SimpleEntity newEntity = new SimpleEntity("New Name", "SUSPENDED", "secret2");

		Map<String, FieldDiff> diffs = AuditDiffEngine.diff(oldEntity, newEntity);

		assertThat(diffs).hasSize(2);
		assertThat(diffs.get("name")).isEqualTo(new FieldDiff("Old Name", "New Name"));
		assertThat(diffs.get("status")).isEqualTo(new FieldDiff("ACTIVE", "SUSPENDED"));
		assertThat(diffs.containsKey("unmonitoredField")).isFalse();
	}

	@Test
	@DisplayName("computes field differences directly at root without 'fields' wrapper")
	void computesFieldDiffs() {
		Map<String, Object> oldFields = Map.of(
				"name", "RS Lama",
				"status", "ACTIVE",
				"unchanged", "same");

		Map<String, Object> newFields = Map.of(
				"name", "RS Baru",
				"status", "SUSPENDED",
				"unchanged", "same",
				"newProp", "extra");

		Map<String, FieldDiff> diffs = AuditDiffEngine.diffFields(oldFields, newFields);

		assertThat(diffs).hasSize(3);
		assertThat(diffs.get("name")).isEqualTo(new FieldDiff("RS Lama", "RS Baru"));
		assertThat(diffs.get("status")).isEqualTo(new FieldDiff("ACTIVE", "SUSPENDED"));
		assertThat(diffs.get("newProp")).isEqualTo(new FieldDiff(null, "extra"));
		assertThat(diffs.containsKey("unchanged")).isFalse();

		String json = AuditJsonFormatter.formatDiff(diffs);
		assertThat(json).doesNotContain("\"fields\":");
		assertThat(json).contains("\"name\":{\"old\":\"RS Lama\",\"new\":\"RS Baru\"}");
		assertThat(json).contains("\"newProp\":{\"old\":null,\"new\":\"extra\"}");
		assertThat(json).contains("\"status\":{\"old\":\"ACTIVE\",\"new\":\"SUSPENDED\"}");
	}

	@Test
	@DisplayName("computes keyed collection differences retaining collection name without 'collections' or 'fields' wrapper")
	void computesKeyedCollectionDiffs() {
		List<Setting> oldSettings = List.of(
				new Setting("org.locale", "en-US", 1, "meta1"),
				new Setting("org.timezone", "UTC", 1, "meta2"),
				new Setting("legacy.key", "oldVal", 1, "meta3"));

		List<Setting> newSettings = List.of(
				new Setting("org.locale", "id-ID", 2, "meta4"),
				new Setting("org.timezone", "UTC", 1, "meta2"), // unchanged
				new Setting("new.setting", "val", 1, "meta5")); // added

		CollectionDiff<Setting> diff = AuditDiffEngine.diffKeyedCollection(
				oldSettings,
				newSettings,
				Setting::key);

		assertThat(diff.hasChanges()).isTrue();
		assertThat(diff.added()).hasSize(1);
		assertThat(diff.added().get(0).key()).isEqualTo("new.setting");

		assertThat(diff.removed()).hasSize(1);
		assertThat(diff.removed().get(0).key()).isEqualTo("legacy.key");

		assertThat(diff.changed()).hasSize(1);
		assertThat(diff.changed().get(0).key()).isEqualTo("org.locale");
		assertThat(diff.changed().get(0).fields().get("value")).isEqualTo(new FieldDiff("en-US", "id-ID"));
		assertThat(diff.changed().get(0).fields().get("revision")).isEqualTo(new FieldDiff(1, 2));

		String json = AuditJsonFormatter.formatCollectionDiff(
				"settings",
				diff,
				s -> "{\"key\":\"%s\",\"value\":\"%s\",\"revision\":%d}".formatted(s.key(), s.value(), s.revision()));

		assertThat(json).doesNotContain("\"collections\":");
		assertThat(json).contains("\"settings\":{");
		assertThat(json).contains("\"added\":[{\"key\":\"new.setting\",\"value\":\"val\",\"revision\":1}]");
		assertThat(json).contains("\"removed\":[{\"key\":\"legacy.key\",\"value\":\"oldVal\",\"revision\":1}]");
		assertThat(json).contains("\"changed\":[{\"key\":\"org.locale\",\"revision\":{\"old\":1,\"new\":2},\"value\":{\"old\":\"en-US\",\"new\":\"id-ID\"}}]");
	}

	@Test
	@DisplayName("computes primitive collection differences")
	void computesPrimitiveCollectionDiffs() {
		List<String> oldRoles = List.of("ROLE_USER", "ROLE_DOCTOR");
		List<String> newRoles = List.of("ROLE_USER", "ROLE_ADMIN");

		CollectionDiff<String> diff = AuditDiffEngine.diffPrimitiveCollection(oldRoles, newRoles);

		assertThat(diff.added()).containsExactly("ROLE_ADMIN");
		assertThat(diff.removed()).containsExactly("ROLE_DOCTOR");
		assertThat(diff.changed()).isEmpty();
	}
}
