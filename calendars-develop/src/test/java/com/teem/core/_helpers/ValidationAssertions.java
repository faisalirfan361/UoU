package com.UoU.core._helpers;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForInterfaceTypes.fail;

import com.UoU.core.validation.ValidatorWrapper;
import com.UoU.core.validation.Violation;
import com.UoU.core.validation.ViolationException;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;
import lombok.val;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.ThrowableAssert;

public class ValidationAssertions {
  private static final ValidatorWrapper VALIDATOR = ValidatorWrapperFactory.createRealInstance();

  public static <T> void assertThatValidationPasses(T object) {
    try {
      VALIDATOR.validateAndThrow(object);
    } catch (ConstraintViolationException ex) {
      var constraints = ex.getConstraintViolations().stream()
          .map(x -> x.getPropertyPath() + ": " + x.getMessage())
          .collect(Collectors.joining(", "));
      fail("Validation failed -> " + constraints, ex);
    } catch (Exception ex) {
      fail("Validation failed", ex);
      throw ex;
    }
  }

  public static <T> AbstractThrowableAssert<
      ? extends AbstractThrowableAssert<?, ConstraintViolationException>,
      ConstraintViolationException> assertThatValidationFails(
      Set<String> invalidProps, T object) {

    val throwableAssert = assertThatValidationFails(() -> VALIDATOR.validateAndThrow(object));

    throwableAssert
        .extracting(x -> x.getConstraintViolations())
        .extracting(x -> x.stream().map(y -> y.getPropertyPath().toString()).collect(toSet()))
        .matches(x -> x.equals(invalidProps), invalidProps.toString());

    return throwableAssert;
  }

  @SuppressWarnings("unchecked")
  public static AbstractThrowableAssert<
      ? extends AbstractThrowableAssert<?, ConstraintViolationException>,
      ConstraintViolationException> assertThatValidationFails(
      ThrowableAssert.ThrowingCallable callable) {

    val throwableAssert = assertThatCode(callable)
        .as("Expecting constraint violations")
        .isInstanceOf(ConstraintViolationException.class);

    throwableAssert
        .extracting(x -> (ConstraintViolationException) x)
        .extracting(x -> x.getConstraintViolations())
        .matches(x -> !x.isEmpty(), "violations are not empty");

    return (AbstractThrowableAssert<
        ? extends AbstractThrowableAssert<?, ConstraintViolationException>,
        ConstraintViolationException>) throwableAssert;
  }

  public static void assertViolationExceptionForField(
      ThrowableAssert.ThrowingCallable callable, String field, String violationMessageContains) {
    assertThatCode(callable)
        .isInstanceOf(ViolationException.class)
        .extracting(x -> ((ViolationException) x).getViolations())
        .asList()
        .as("Should have one matching violation for field")
        .hasSize(1)
        .first()
        .matches(
            x -> ((Violation) x).field().equals(field),
            "Violation field should match")
        .matches(
            x -> ((Violation) x).message().toLowerCase().contains(violationMessageContains),
            "Violation message should contain expected string");
  }
}
