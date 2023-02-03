package com.UoU.core.events;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationPasses;

import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OwnerTests {
  @Test
  void validation_shouldPass() {
    assertThatValidationPasses(new Owner(null, "a@b.co"));
  }

  @ParameterizedTest
  @MethodSource
  void validation_shouldFail(Set<String> invalidProps, Owner owner) {
    assertThatValidationFails(invalidProps, owner);
  }

  private static Stream<Arguments> validation_shouldFail() { // test data
    return Stream.of(
        Arguments.of(Set.of("email"), new Owner(null, "")),
        Arguments.of(Set.of("email"), new Owner(null, " ")),
        Arguments.of(Set.of("email"), new Owner(null, "invalid"))
    );
  }
}
