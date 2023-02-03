package com.UoU.core.calendars;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationPasses;

import com.UoU._helpers.TestData;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CalendarCreateRequestTests {

  @Test
  void validation_shouldPass() {
    assertThatValidationPasses(buildValid().build());
  }

  @ParameterizedTest
  @MethodSource
  void validation_shouldFail(Set<String> invalidProps, CalendarCreateRequest request) {
    assertThatValidationFails(invalidProps, request);
  }

  private static Stream<Arguments> validation_shouldFail() { // test data
    return Stream.of(
        Arguments.of(
            Set.of("id", "orgId"),
            CalendarCreateRequest.builder().build()), // everything is null
        Arguments.of(
            Set.of("externalId.value"),
            buildValid().externalId(new CalendarExternalId("")).build()),
        Arguments.of(
            Set.of("name"),
            buildValid().name("x".repeat(CalendarConstraints.NAME_MAX + 1)).build()),
        Arguments.of(
            Set.of("timezone"),
            buildValid().timezone("invalid").build())
    );
  }

  private static CalendarCreateRequest.Builder buildValid() {
    return CalendarCreateRequest.builder()
        .id(CalendarId.create())
        .orgId(TestData.orgId())
        .name("Test")
        .timezone("America/Denver")
        .isReadOnly(false);
  }
}
