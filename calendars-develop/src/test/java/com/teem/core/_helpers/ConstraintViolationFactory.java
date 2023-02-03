package com.UoU.core._helpers;

import com.UoU.core.validation.ValidatorWrapper;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import lombok.val;

public class ConstraintViolationFactory {
  private static final ValidatorWrapper VALIDATOR = ValidatorWrapperFactory.createRealInstance();

  /**
   * Creates a violation by actually validating the passed object (so it must be invalid).
   *
   * <p>This is useful because the violation will be a real one, and better for testing, as
   * opposed to mocking ConstraintViolation or faking the interface.
   */
  public static Set<ConstraintViolation<?>> createViolations(Object value) {
    try {
      VALIDATOR.validateAndThrow(value);
      throw new RuntimeException("Expected ConstraintViolationException");
    } catch (ConstraintViolationException ex) {
      return ex.getConstraintViolations();
    }
  }

  public static ConstraintViolation<?> createSingleViolation(Object value) {
    val violations = createViolations(value);
    if (violations.size() != 1) {
      throw new RuntimeException(
          "Expected a single ConstraintViolationException but got " + violations.size());
    }
    return violations.stream().findFirst().orElseThrow();
  }
}
