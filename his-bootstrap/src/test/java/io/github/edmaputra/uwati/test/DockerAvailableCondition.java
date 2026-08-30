package io.github.edmaputra.uwati.test;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

/**
 * JUnit 5 {@link ExecutionCondition} that checks if a Docker/container runtime is reachable.
 * <p>
 * If Docker is unavailable, tests annotated with {@link RequiresDocker} are gracefully skipped
 * rather than failing the build.
 */
public class DockerAvailableCondition implements ExecutionCondition {

	private static final ConditionEvaluationResult ENABLED =
			ConditionEvaluationResult.enabled("Docker environment is available");

	private static final ConditionEvaluationResult DISABLED =
			ConditionEvaluationResult.disabled("Docker environment is not available; skipping container integration test");

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		try {
			if (DockerClientFactory.instance().isDockerAvailable()) {
				return ENABLED;
			}
		}
		catch (Throwable ignored) {
		}
		return DISABLED;
	}
}
