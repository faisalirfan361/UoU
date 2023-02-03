package com.UoU;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.library.GeneralCodingRules;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tests that verify architecture rules using ArchUnit.
 * See https://www.archunit.org/userguide/html/000_Index.
 *
 * <p>This is not a comprehensive list of rules. It just checks things that are common accidents
 * to help us stick to our agreed-upon conventions more easily.
 *
 * <p>Tests and generated code are excluded from checks.
 */
@AnalyzeClasses(
    packages = {"com.UoU"},
    importOptions = {
        ImportOption.DoNotIncludeTests.class,
        ArchitectureTests.ExcludeGenerated.class,
    })
class ArchitectureTests {
  private static final String APP_PKG = "com.UoU.app";
  private static final String CORE_PKG = "com.UoU.core";
  private static final String INFRA_PKG = "com.UoU.infra";

  /**
   * Classes in com.UoU.core should only depend on com.UoU.core, standard java/kotlin stuff,
   * and a very few other things. Core is meant to contain the domain model and business logic,
   * so we don't want to pollute it with too many infra concerns.
   */
  @ArchTest
  private final ArchRule coreShouldDependOnlyOnCoreAndBaseStuff = ArchRuleDefinition
      .classes().that().resideInAPackage(CORE_PKG + "..")
      .should().onlyDependOnClassesThat().resideInAnyPackage(
          CORE_PKG + "..",
          "java..", // std lib stuff
          "org.jetbrains.annotations..",
          "org.springframework.stereotype..", "org.springframework..annotation..",
          "org.springframework.boot.context.properties", // ConfigurationProperties
          "lombok..", "javax.annotation..",
          "javax.validation..", "org.springframework.validation..", // validation
          "org.hibernate.validator.constraints.time", // constraints not available in javax..
          "org.apache.commons.lang3..", // helpers for things java is missing
          "org.apache.commons.collections..", // CollectionUtils and other helpers
          "org.slf4j..", // logging
          "com.nylas..", // nylas sdk
          "com.microsoft.graph..", // ms graph sdk for Teams meetings
          "okhttp3", // required for ms graph sdk, only to be used inside TeamsService
          "org.mapstruct..", // type mapping
          "org.dmfs.rfc5545..", // recurrence rrule lib
          "org.springframework.scheduling..", // spring scheduler
          "org.springframework.util..", // StringUtils, concurrency utils, and other helpers
          "org.springframework.web.client", // RestTemplate for outgoing http requests
          "org.springframework.lang" // Nullable annotation
      );

  /**
   * Classes in com.UoU.app should not depend directly on the infra db layer or jooq. Rather,
   * they use the repository and service abstractions from com.UoU.core. The application layer is
   * meant to orchestrate the other layers and so should not be doing low-level db stuff.
   * The exception to this rule is where beans are configured for dependency injection.
   */
  @ArchTest
  private final ArchRule appShouldNotDependDirectlyOnDbExceptForConfiguration = ArchRuleDefinition
      .noClasses().that().resideInAPackage(APP_PKG + "..")
      .and().areNotAnnotatedWith(SpringBootApplication.class)
      .and().areNotAnnotatedWith(Configuration.class)
      .and().areNotAnnotatedWith(ControllerAdvice.class)
      .should().dependOnClassesThat().resideInAnyPackage(INFRA_PKG + ".db..", "org.jooq..");

  /**
   * Classes in com.UoU.infra should not depend on com.UoU.app. The dependency should only go
   * the other way because the app uses infra to orchestrate and present the app to users.
   */
  @ArchTest
  private final ArchRule infraShouldNotDependOnApp = ArchRuleDefinition
      .noClasses().that().resideInAPackage(INFRA_PKG + "..")
      .should().dependOnClassesThat().resideInAPackage(APP_PKG + "..");

  /**
   * App controllers should be named with "Controller" suffix.
   */
  @ArchTest
  private final ArchRule appControllersShouldHaveSuffix = ArchRuleDefinition
      .classes().that().resideInAPackage(APP_PKG + "..")
      .and().areAnnotatedWith(RestController.class)
      .should().haveSimpleNameEndingWith("Controller");

  /**
   * App DTOs should use @Schema annotations, not things like @NotNull, @Nullable, @Size, etc.
   */
  @ArchTest
  private final ArchRule appDtosShouldNotUseUnapprovedAnnotations = ArchRuleDefinition
      .noClasses().that().resideInAPackage(APP_PKG + "..dtos..")
      .should()
      .dependOnClassesThat().resideInAnyPackage(
          "javax.validation..",
          "javax.annotation..",
          "org.springframework.lang.."
      );

  /**
   * Don't use unapproved non-null or nullable attributes. Instead, use
   * javax.validation.constraints.NotNull for most cases and lombok.NonNull more sparingly where
   * you need a runtime null-check. For the rare cases you need @Nullable, use only
   * org.springframework.lang.Nullable.
   *
   * <p>Note: There are lots more non-null/nullable variations floating around in Java-world, but
   * these are the ones that are likely to be imported by accident without noticing.
   */
  @ArchTest
  private final ArchRule shouldNotUseUnapprovedNullRelatedAttributes = ArchRuleDefinition
      .noClasses()
      .should()
      .dependOnClassesThat(new DescribedPredicate<>("are unapproved non-null/nullable attributes") {
        private static final Set<String> ATTRS = Set.of(
            // invalid non-nulls:
            "javax.annotation.Nonnull",
            "org.springframework.lang.NonNull",
            "org.jetbrains.annotations.NotNull",
            "org.eclipse.jdt.annotation.NonNull",
            "io.reactivex.annotations.NonNull",
            "io.reactivex.rxjava3.annotations.NonNull",
            // invalid nullables:
            "javax.annotation.Nullable",
            "javax.annotation.CheckForNull",
            "org.jetbrains.annotations.Nullable",
            "org.eclipse.jdt.annotation.Nullable",
            "io.reactivex.annotations.Nullable",
            "io.reactivex.rxjava3.annotations.Nullable"
        );

        @Override
        public boolean test(JavaClass input) {
          return ATTRS.contains(input.getFullName());
        }
      });

  /**
   * Don't use fields for dependency injection. Use constructor injection instead.
   */
  @ArchTest
  private final ArchRule shouldNotUseFieldInjection = GeneralCodingRules
      .NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

  /**
   * Don't throw generic exceptions. Instead, use something more specific.
   */
  @ArchTest
  private final ArchRule shouldNotThrowGenericExceptions = GeneralCodingRules
      .NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;

  /**
   * Don't use java util logging. Use slf4j only.
   */
  @ArchTest
  private final ArchRule shouldNotUseJavaUtilLogging = GeneralCodingRules
      .NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

  /**
   * Don't write to standard error or standard out.
   */
  @ArchTest
  private final ArchRule shouldNotUseStandardErrAndOut = GeneralCodingRules
      .NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

  protected static class ExcludeGenerated implements ImportOption {
    private static final Pattern PATTERN = Pattern.compile(".*/com/UoU/infra/(avro|jooq)/.*");

    @Override
    public boolean includes(Location location) {
      return !location.matches(PATTERN);
    }
  }
}
