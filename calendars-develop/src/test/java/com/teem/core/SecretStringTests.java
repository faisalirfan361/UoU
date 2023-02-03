package com.UoU.core;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import java.util.Set;
import org.junit.jupiter.api.Test;

class SecretStringTests {

  @Test
  void ctor_shouldThrowForNullValue() {
    assertThatCode(() -> new SecretString(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void validation_shouldFailForBlankValue() {
    assertThatValidationFails(Set.of("value"), new SecretString(" "));
  }

  @Test
  void toString_shouldObscureValue() {
    var secret = "my-secret";
    var result = new SecretString(secret).toString();
    assertThat(result).doesNotContain(secret);
  }
}
