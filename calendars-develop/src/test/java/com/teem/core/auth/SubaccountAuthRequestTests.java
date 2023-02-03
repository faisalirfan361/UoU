package com.UoU.core.auth;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationPasses;

import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU.core.OrgId;
import com.UoU.core.auth.SubaccountAuthRequest;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SubaccountAuthRequestTests {

  @Test
  void validation_shouldPass() {
    var request = buildValid().build();
    assertThatValidationPasses(request);
  }

  @ParameterizedTest
  @MethodSource
  void validation_shouldFail(Set<String> invalidProps, SubaccountAuthRequest request) {
    assertThatValidationFails(invalidProps, request);
  }

  private static Stream<Arguments> validation_shouldFail() { // test data
    return Stream.of(
        Arguments.of(
            Set.of("serviceAccountId", "orgId", "email", "name"),
            ModelBuilders.subaccountAuthRequest()
                .build()), // everything is null
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
            Set.of("name"),
            buildValid()
                .name("")
                .build())
    );
  }

  private static ModelBuilders.SubaccountAuthRequestBuilder buildValid() {
    return ModelBuilders.subaccountAuthRequest()
        .serviceAccountId(TestData.serviceAccountId())
        .orgId(TestData.orgId())
        .email(TestData.email())
        .name("Test Account");
  }
}
