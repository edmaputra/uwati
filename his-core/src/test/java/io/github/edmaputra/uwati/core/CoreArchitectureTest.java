package io.github.edmaputra.uwati.core;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Architectural fitness tests verifying Hexagonal Architecture boundaries in his-core.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@AnalyzeClasses(
		packages = "io.github.edmaputra.uwati.core",
		importOptions = ImportOption.DoNotIncludeTests.class
)
class CoreArchitectureTest {

	@ArchTest
	static final ArchRule core_must_not_depend_on_adapters =
			noClasses().that().resideInAPackage("..core..")
					.should().dependOnClassesThat()
					.resideInAnyPackage("..adapter..", "..cache..", "..bootstrap..");

	@ArchTest
	static final ArchRule core_must_not_use_spring_service_or_component_annotations =
			noClasses().that().resideInAPackage("..core..")
					.should().beAnnotatedWith("org.springframework.stereotype.Service")
					.orShould().beAnnotatedWith("org.springframework.stereotype.Component");
}
