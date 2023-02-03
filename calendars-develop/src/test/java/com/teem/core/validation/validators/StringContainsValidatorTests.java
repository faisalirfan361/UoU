package com.UoU.core.validation.validators;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationPasses;

import com.UoU.core.validation.annotations.StringContains;
import java.util.Set;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class StringContainsValidatorTests {

  @ParameterizedTest
  @ValueSource(strings = {
      TestObj.EXPECTED,
      "prefix" + TestObj.EXPECTED + "suffix",
  })
  void shouldPass(String value) {
    assertThatValidationPasses(new TestObj(value));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "null",
      "",
      " ",
      " does not contain EXPECTED VALUE because case is different",
  })
  void shouldFailOnInvalidValues(String value) {
    assertThatValidationFails(Set.of("value"), new TestObj(value.equals("null") ? null : value));
  }

  private record TestObj(@StringContains(EXPECTED) String value) {
    public static final String EXPECTED = "expected value";
  }
}
