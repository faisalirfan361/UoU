package com.UoU.core.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Helps running validation checks and adding violations to {@link ConstraintValidatorContext}.
 */
public class ValidatorChecker<T> {
  private final List<Check> checks = new ArrayList<>();
  @Setter private boolean disableDefaultConstraintViolation = true;

  /**
   * Adds a validation check.
   *
   * @param isValid func that should return true if the value passes the validation check.
   * @param propertyName property that should be marked invalid if isValid returns false.
   * @param failureMessage message that describes the validation failure.
   */
  public ValidatorChecker<T> add(
      BiFunction<T, ConstraintValidatorContext, Boolean> isValid,
      String propertyName,
      String failureMessage) {

    checks.add(new Check(isValid, propertyName, x -> failureMessage));
    return this;
  }

  /**
   * Adds a validation check.
   *
   * @param isValid func that should return true if the value passes the validation check.
   * @param propertyName property that should be marked invalid if isValid returns false.
   * @param failureMessageSupplier supplier that lazy-creates a message to describe the failure.
   */
  public ValidatorChecker<T> add(
      BiFunction<T, ConstraintValidatorContext, Boolean> isValid,
      String propertyName,
      Function<T, String> failureMessageSupplier) {

    checks.add(new Check(isValid, propertyName, failureMessageSupplier));
    return this;
  }

  /**
   * Adds a validation check.
   *
   * @param isValid func that should return true if the value passes the validation check.
   * @param propertyNamesAndFailureMessageSuppliers pairs of property names and failure messages.
   */
  @SafeVarargs
  public final ValidatorChecker<T> add(
      BiFunction<T, ConstraintValidatorContext, Boolean> isValid,
      Pair<String, Function<T, String>>... propertyNamesAndFailureMessageSuppliers) {

    checks.add(new Check(isValid, Arrays.asList(propertyNamesAndFailureMessageSuppliers)));
    return this;
  }

  /**
   * Runs all validation checks and adds constraint violations to the context as needed.
   */
  public boolean isValid(T value, ConstraintValidatorContext context) {
    var isValid = true;

    for (val check : checks) {
      Boolean result;
      var customMessage = Optional.<String>empty();

      try {
        result = check.getIsValid().apply(value, context);
      } catch (IsValidCheckException ex) {
        result = false;
        customMessage = Optional.ofNullable(ex.getMessage());
      }

      if (result == null || !result) {
        isValid = false;
        for (val propertyAndMessage : check.getPropertyNamesAndFailureMessageSuppliers()) {
          context
              .buildConstraintViolationWithTemplate(
                  customMessage
                      .filter(x -> !x.isBlank())
                      .orElseGet(() -> propertyAndMessage.getRight().apply(value)))
              .addPropertyNode(propertyAndMessage.getLeft())
              .addConstraintViolation();
        }
      }
    }

    if (!isValid && disableDefaultConstraintViolation) {
      context.disableDefaultConstraintViolation();
    }

    return isValid;
  }

  /**
   * Throws an exception to interrupt isValid function processing and return a custom error message.
   *
   * <p>This is intended to be used inside isValid check functions only. Normally, isValid functions
   * return false to indicate failure, and then the default error message is used to build the
   * constraint violation(s). But if this is called, the function will be aborted and the custom
   * message used for all check properties. This is useful when there are specific failures where
   * the default message isn't good enough.
   *
   * <p>Example:
   * <pre>{@code
   *   checker.add(
   *     (val, ctx) -> {
   *       if (val.equals("something")) {
   *         return ValidationChecker.failIsValidCheck("Some custom message");
   *       }
   *       return false; // fail with default error message
   *     },
   *     "propName",
   *     "default error message");
   * }</pre>
   */
  public static boolean failIsValidCheck(String message) {
    throw new IsValidCheckException(message);
  }

  @Value
  @AllArgsConstructor
  private class Check {
    BiFunction<T, ConstraintValidatorContext, Boolean> isValid;
    List<Pair<String, Function<T, String>>> propertyNamesAndFailureMessageSuppliers;

    public Check(
        BiFunction<T, ConstraintValidatorContext, Boolean> isValid,
        String propertyName,
        Function<T, String> failureMessageSupplier) {
      this.isValid = isValid;
      this.propertyNamesAndFailureMessageSuppliers = List.of(Pair.of(
          propertyName,
          failureMessageSupplier));
    }
  }

  private static class IsValidCheckException extends RuntimeException {
    public IsValidCheckException(String message) {
      super(message);
    }
  }
}
