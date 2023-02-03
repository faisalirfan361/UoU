package com.UoU.core.validation.validators;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationPasses;

import com.UoU.core.validation.annotations.NotZero;
import java.util.Set;
import org.junit.jupiter.api.Test;

class NotZeroValidatorTests {

  @Test
  void shouldPassWithAllNumericTypesThatAreNull() {
    assertThatValidationPasses(new TestObj(null, null, null, null, null));
  }

  @Test
  void shouldPassWithAllNumericTypesThatAreNonZero() {
    assertThatValidationPasses(new TestObj(1, 1L, 1F, 1D, (short) 1));
  }

  @Test
  void shouldFailWithAllNumericTypesThatAreZero() {
    assertThatValidationFails(
        Set.of("intValue", "longValue", "floatValue", "doubleValue", "shortValue"),
        new TestObj(0, 0L, 0F, 0D, (short) 0));
  }

  private record TestObj(
      @NotZero Integer intValue,
      @NotZero Long longValue,
      @NotZero Float floatValue,
      @NotZero Double doubleValue,
      @NotZero Short shortValue) {
  }
}
