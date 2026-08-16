package com.flower_details.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.flower_details", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

	@ArchTest
	static final ArchRule domain_does_not_depend_on_outer_layers_or_frameworks = noClasses()
			.that().resideInAnyPackage("..domain..")
			.should().dependOnClassesThat().resideInAnyPackage(
					"..application..",
					"..presentation..",
					"..infrastructure..",
					"org.springframework..",
					"jakarta.persistence..",
					"org.hibernate.."
			);

	@ArchTest
	static final ArchRule application_does_not_depend_on_presentation_or_infrastructure = noClasses()
			.that().resideInAnyPackage("..application..")
			.should().dependOnClassesThat().resideInAnyPackage("..presentation..", "..infrastructure..");

	@ArchTest
	static final ArchRule presentation_does_not_depend_on_persistence = noClasses()
			.that().resideInAnyPackage("..presentation..")
			.should().dependOnClassesThat().resideInAnyPackage(
					"..domain.repository..",
					"..infrastructure.persistence.."
			);
}
