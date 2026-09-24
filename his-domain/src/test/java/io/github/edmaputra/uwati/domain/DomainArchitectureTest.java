package io.github.edmaputra.uwati.domain;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Architectural fitness tests verifying pure Java domain invariants in his-domain.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@AnalyzeClasses(
		packages = "io.github.edmaputra.uwati.domain",
		importOptions = ImportOption.DoNotIncludeTests.class
)
class DomainArchitectureTest {

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

	@ArchTest
	static final ArchRule domain_must_not_depend_on_application_or_adapters =
			noClasses().that().resideInAPackage("..domain..")
					.should().dependOnClassesThat()
					.resideInAnyPackage("..core..", "..adapter..", "..cache..", "..bootstrap..");
}
