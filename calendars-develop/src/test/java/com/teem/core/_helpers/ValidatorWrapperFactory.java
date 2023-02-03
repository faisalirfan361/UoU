package com.UoU.core._helpers;

import com.UoU.core.validation.ValidatorWrapper;
import javax.validation.Validation;

/**
 * Helper to create {@link ValidatorWrapper} instances for testing.
 */
public class ValidatorWrapperFactory {

  /**
   * Creates a real {@link ValidatorWrapper} for cases where actual validation should be tested.
   */
  public static ValidatorWrapper createRealInstance() {
    return new ValidatorWrapper(Validation.buildDefaultValidatorFactory().getValidator());
  }
}
