package io.github.edmaputra.uwati.domain.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UuidV7Test {

	@Test
	@DisplayName("Should generate valid UUIDv7 with version 7 and RFC variant")
	void shouldGenerateValidUuidV7() {
		UUID uuid = UuidV7.generate();

		assertThat(uuid).isNotNull();
		assertThat(uuid.version()).isEqualTo(7);
		assertThat(uuid.variant()).isEqualTo(2); // RFC 4122 / 9562
	}

	@Test
	@DisplayName("Should preserve and accurately extract encoded timestamp")
	void shouldPreserveTimestamp() {
		Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
		UUID uuid = UuidV7.generate(now);

		assertThat(uuid.version()).isEqualTo(7);
		Instant extracted = UuidV7.extractTimestamp(uuid);
		assertThat(extracted).isEqualTo(now);
	}

	@Test
	@DisplayName("Should generate monotonic time-ordered sequence")
	void shouldGenerateTimeOrderedSequence() throws InterruptedException {
		List<UUID> uuids = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			uuids.add(UuidV7.generate());
			Thread.sleep(2);
		}

		for (int i = 0; i < uuids.size() - 1; i++) {
			UUID current = uuids.get(i);
			UUID next = uuids.get(i + 1);
			assertThat(current.compareTo(next)).isLessThan(0);
		}
	}

	@Test
	@DisplayName("Should reject extracting timestamp from non-v7 UUID")
	void shouldRejectNonV7Extraction() {
		UUID v4 = UUID.randomUUID();
		assertThatThrownBy(() -> UuidV7.extractTimestamp(v4))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Expected UUID version 7");
	}
}
