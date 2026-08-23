package io.github.edmaputra.uwati.bootstrap.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ScopedValueAuditContext Unit Tests")
class ScopedValueAuditContextTests {

	private final ScopedValueAuditContext context = new ScopedValueAuditContext();

	@Test
	@DisplayName("returns empty and defaults when outside an audit scope")
	void returnsEmptyOutsideScope() {
		assertThat(context.currentActor()).isEmpty();
		assertThat(context.currentCorrelationId()).isEmpty();
		assertThat(context.requireActor()).isEqualTo("system");
		assertThat(context.requireCorrelationId()).isEqualTo("unknown");
	}

	@Test
	@DisplayName("binds and retrieves actor and correlation ID within scope")
	void bindsActorAndCorrelationIdInScope() {
		context.callWithAuditContext("operator-123", "corr-abc-456", () -> {
			assertThat(context.currentActor()).contains("operator-123");
			assertThat(context.currentCorrelationId()).contains("corr-abc-456");
			assertThat(context.requireActor()).isEqualTo("operator-123");
			assertThat(context.requireCorrelationId()).isEqualTo("corr-abc-456");
			return null;
		});

		// Cleared after scope exit
		assertThat(context.currentActor()).isEmpty();
		assertThat(context.currentCorrelationId()).isEmpty();
	}

	@Test
	@DisplayName("falls back to default strings when null or blank values are provided")
	void fallsBackToDefaultsOnNullOrBlank() {
		context.callWithAuditContext("  ", null, () -> {
			assertThat(context.requireActor()).isEqualTo("system");
			assertThat(context.requireCorrelationId()).isEqualTo("unknown");
			return null;
		});
	}

	@Test
	@DisplayName("rejects null operation")
	void rejectsNullOperation() {
		assertThatNullPointerException()
				.isThrownBy(() -> context.callWithAuditContext("user", "id", null));
	}
}
