package com.UoU.core.accounts;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationPasses;

import com.UoU._helpers.TestData;
import com.UoU.core.OrgId;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ServiceAccountUpdateRequestTests {
  @Test
  void validation_shouldPass() {
    var request = buildValid().build();
    assertThatValidationPasses(request);
  }

  @ParameterizedTest
  @MethodSource
  void validation_shouldFail(Set<String> invalidProps, ServiceAccountUpdateRequest request) {
    assertThatValidationFails(invalidProps, request);
  }

  private static Stream<Arguments> validation_shouldFail() { // test data
    return Stream.of(
        Arguments.of(
            Set.of("settings", "id", "orgId"),
            ServiceAccountUpdateRequest.builder().build()), // everything is null
        Arguments.of(
            Set.of("orgId.value"),
            buildValid()
                .orgId(new OrgId(" "))
                .build()),
        Arguments.of(
            Set.of("settings"),
            buildValid()
                .settings(null)
                .build())
    );
  }

  private static ServiceAccountUpdateRequest.Builder buildValid() {
    return ServiceAccountUpdateRequest.builder()
        .id(ServiceAccountId.create())
        .orgId(TestData.orgId())
        .settings(new HashMap<>());
  }
}
