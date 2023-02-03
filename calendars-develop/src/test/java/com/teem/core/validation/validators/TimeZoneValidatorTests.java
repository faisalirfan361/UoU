package com.UoU.core.validation.validators;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationPasses;

import com.UoU.core.validation.annotations.TimeZone;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TimeZoneValidatorTests {

  @Test
  void shouldAllowNull() {
    assertThatValidationPasses(new TestObj(null));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "America/Denver",
      "UTC",
      "MST",
      "EST",
      "GMT+0",
      "Etc/Zulu",
      "Etc/GMT+11"
  })
  void shouldPassOnValidTimezones(String timezone) {
    assertThatValidationPasses(new TestObj(timezone));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "",
      " ",
      "invalid",
      " America/Chicago",
      "-20"
  })
  void shouldFailOnInvalidTimezones(String timezone) {
    assertThatValidationFails(Set.of("timezone"), new TestObj(timezone));
  }

  private record TestObj(@TimeZone String timezone) {
  }
}
