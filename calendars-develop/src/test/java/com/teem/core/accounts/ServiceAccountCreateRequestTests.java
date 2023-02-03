package com.UoU.core.accounts;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationPasses;

import com.UoU._helpers.TestData;
import com.UoU.core.OrgId;
import com.UoU.core.auth.AuthMethod;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ServiceAccountCreateRequestTests {
  @Test
  void validation_shouldPass() {
    var request = buildValid().build();
    assertThatValidationPasses(request);
  }

  @ParameterizedTest
  @MethodSource
  void validation_shouldFail(Set<String> invalidProps, ServiceAccountCreateRequest request) {
    assertThatValidationFails(invalidProps, request);
  }

  private static Stream<Arguments> validation_shouldFail() { // test data
    return Stream.of(
        Arguments.of(
            Set.of("settings", "id", "orgId", "email", "authMethod"),
            ServiceAccountCreateRequest.builder().build()), // everything is null
        Arguments.of(
            Set.of("orgId.value"),
            buildValid()
                .orgId(new OrgId(" "))
                .build()),
        Arguments.of(
            Set.of("email"),
            buildValid()
                .email("x")
                .build()),
        Arguments.of(
            Set.of("email"),
            buildValid()
                .email("")
                .build()),
        Arguments.of(
            Set.of("email"),
            buildValid()
                .email(" ")
                .build()),
        Arguments.of(Set.of("authMethod"),
            buildValid()
                .authMethod(AuthMethod.GOOGLE_OAUTH) // not a service account method
                .build()));
  }

  private static ServiceAccountCreateRequest.Builder buildValid() {
    return ServiceAccountCreateRequest.builder()
        .id(ServiceAccountId.create())
        .orgId(TestData.orgId())
        .email(TestData.email())
        .settings(new HashMap<>())
        .authMethod(AuthMethod.MS_OAUTH_SA);
  }
}
