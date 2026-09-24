package io.github.edmaputra.uwati;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Architectural fitness tests verifying Hexagonal Architecture (Ports and Adapters) boundaries
 * and layer dependency rules across the Uwati HIS project.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@AnalyzeClasses(
		packages = "io.github.edmaputra.uwati",
		importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureTest {

	// --- Domain layer must have ZERO framework dependencies ---

	@ArchTest
	static final ArchRule domain_must_not_depend_on_spring =
			noClasses().that().resideInAPackage("..domain..")
					.should().dependOnClassesThat()
					.resideInAnyPackage("org.springframework..");

	@ArchTest
	static final ArchRule domain_must_not_depend_on_jpa =
			noClasses().that().resideInAPackage("..domain..")
					.should().dependOnClassesThat()
					.resideInAnyPackage("jakarta.persistence..", "org.hibernate..");

	// --- Domain must not depend on application, adapters, or cache ---

	@ArchTest
	static final ArchRule domain_must_not_depend_on_application_or_adapters =
			noClasses().that().resideInAPackage("..domain..")
					.should().dependOnClassesThat()
					.resideInAnyPackage("..core..", "..adapter..", "..cache..", "..bootstrap..");

	// --- Application (his-core) must not depend on adapters or bootstrap ---

	@ArchTest
	static final ArchRule application_must_not_depend_on_adapters =
			noClasses().that().resideInAPackage("..core..")
					.should().dependOnClassesThat()
			.resideInAnyPackage("..adapter..", "..cache..", "..bootstrap..");

	// --- Adapters must not depend on each other ---

	@ArchTest
	static final ArchRule adapters_must_not_depend_on_each_other =
			slices().matching("..adapter.(*)..")
					.should().notDependOnEachOther();

	// --- Controllers must not access persistence repositories directly ---

	@ArchTest
	static final ArchRule controllers_must_not_access_persistence =
			noClasses().that().resideInAPackage("..adapter.rest..")
					.should().dependOnClassesThat()
					.resideInAnyPackage("..adapter.persistence..");
}
