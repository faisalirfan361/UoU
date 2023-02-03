package com.UoU.core.auth;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationPasses;

import com.UoU._helpers.ModelBuilders;
import java.time.Duration;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AuthCodeCreateRequestTests {
  @Test
  void validation_shouldPass() {
    var request = ModelBuilders.authCodeCreateRequestWithTestData();
    assertThatValidationPasses(request);
  }

  @ParameterizedTest
  @MethodSource
  void validation_shouldFail(Set<String> invalidProps, AuthCodeCreateRequest request) {
    assertThatValidationFails(invalidProps, request);
  }

  private static Stream<Arguments> validation_shouldFail() { // test data
    return Stream.of(
        Arguments.of(
            Set.of("code", "orgId", "expiration"),
            ModelBuilders.authCodeCreateRequest()
                .build()), // all required fields are null
        Arguments.of(
            Set.of("expiration"),
            ModelBuilders.authCodeCreateRequestWithTestData()
                .expiration(Duration.ZERO) // cannot be negative or 0
                .build()),
        Arguments.of(
            Set.of("redirectUri"),
            ModelBuilders.authCodeCreateRequestWithTestData()
                .redirectUri("/invalid-url")
                .build()),
        Arguments.of(
            Set.of("redirectUri"),
            ModelBuilders.authCodeCreateRequestWithTestData()
                .redirectUri(
                    "https://" + "x".repeat(AuthConstraints.REDIRECT_URI_MAX - 11) + ".com")
                .build())
    );
  }
}
