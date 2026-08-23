package io.github.edmaputra.uwati.core.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.edmaputra.uwati.core.audit.AuditDiffEngine.CollectionDiff;
import io.github.edmaputra.uwati.core.audit.AuditDiffEngine.FieldDiff;

@DisplayName("AuditDiffEngine & AuditJsonFormatter Unit Tests")
class AuditDiffEngineTests {

	record Setting(String key, String value, int revision) {}

	@Test
	@DisplayName("computes field differences between previous and updated maps")
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
		assertThat(json).contains("\"fields\":{");
		assertThat(json).contains("\"name\":{\"old\":\"RS Lama\",\"new\":\"RS Baru\"}");
		assertThat(json).contains("\"newProp\":{\"old\":null,\"new\":\"extra\"}");
		assertThat(json).contains("\"status\":{\"old\":\"ACTIVE\",\"new\":\"SUSPENDED\"}");
	}

	@Test
	@DisplayName("computes keyed collection differences with added, removed, and changed elements")
	void computesKeyedCollectionDiffs() {
		List<Setting> oldSettings = List.of(
				new Setting("org.locale", "en-US", 1),
				new Setting("org.timezone", "UTC", 1),
				new Setting("legacy.key", "oldVal", 1));

		List<Setting> newSettings = List.of(
				new Setting("org.locale", "id-ID", 2),
				new Setting("org.timezone", "UTC", 1), // unchanged
				new Setting("new.setting", "val", 1)); // added

		CollectionDiff<Setting> diff = AuditDiffEngine.diffKeyedCollection(
				oldSettings,
				newSettings,
				Setting::key,
				(oldS, newS) -> AuditDiffEngine.diffFields(
						Map.of("value", oldS.value(), "revision", oldS.revision()),
						Map.of("value", newS.value(), "revision", newS.revision())));

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

		assertThat(json).contains("\"collections\":{\"settings\":{");
		assertThat(json).contains("\"added\":[{\"key\":\"new.setting\",\"value\":\"val\",\"revision\":1}]");
		assertThat(json).contains("\"removed\":[{\"key\":\"legacy.key\",\"value\":\"oldVal\",\"revision\":1}]");
		assertThat(json).contains("\"changed\":[{\"key\":\"org.locale\",\"fields\":{\"revision\":{\"old\":1,\"new\":2},\"value\":{\"old\":\"en-US\",\"new\":\"id-ID\"}}}]");
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
