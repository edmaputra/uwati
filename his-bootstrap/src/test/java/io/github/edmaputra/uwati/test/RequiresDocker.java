package io.github.edmaputra.uwati.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Annotation for integration tests that require a live Docker daemon or compatible container runtime.
 * <p>
 * Evaluated by {@link DockerAvailableCondition}. If Docker is not available on the host machine,
 * the annotated test class or method is automatically skipped.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DockerAvailableCondition.class)
public @interface RequiresDocker {
}
