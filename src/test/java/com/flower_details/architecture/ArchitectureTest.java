package com.flower_details.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
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
	static final ArchRule application_does_not_depend_on_outer_layers = noClasses()
			.that().resideInAnyPackage("..application..")
			.should().dependOnClassesThat().resideInAnyPackage("..presentation..", "..infrastructure..");

	@ArchTest
	static final ArchRule infrastructure_does_not_depend_on_presentation = noClasses()
			.that().resideInAnyPackage("..infrastructure..")
			.should().dependOnClassesThat().resideInAnyPackage("..presentation..");

	@ArchTest
	static final ArchRule presentation_does_not_depend_on_persistence = noClasses()
			.that().resideInAnyPackage("..presentation..")
			.should().dependOnClassesThat().resideInAnyPackage(
					"..domain.repository..",
					"..infrastructure.persistence.."
			);

	@ArchTest
	static final ArchRule domain_repositories_are_interfaces = classes()
			.that().resideInAnyPackage("..domain.repository..")
			.should().beInterfaces();

	@ArchTest
	static final ArchRule use_cases_are_application_services = classes()
			.that().resideInAnyPackage("..application.usecase..")
			.should().haveSimpleNameEndingWith("UseCase")
			.andShould().beAnnotatedWith(Service.class);

	@ArchTest
	static final ArchRule rest_controllers_are_presentation_adapters = classes()
			.that().resideInAnyPackage("..presentation.controller..")
			.should().beAnnotatedWith(RestController.class);

	@ArchTest
	static final ArchRule application_has_no_legacy_application_services = noClasses()
			.that().resideInAnyPackage("..application..")
			.should().haveSimpleNameEndingWith("ApplicationService");
}
