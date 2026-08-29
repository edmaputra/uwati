package io.github.edmaputra.uwati.test;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

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
