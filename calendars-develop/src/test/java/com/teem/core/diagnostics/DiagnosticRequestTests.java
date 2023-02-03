package com.UoU.core.diagnostics;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationPasses;

import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU.core.OrgId;
import com.UoU.core.calendars.CalendarId;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DiagnosticRequestTests {

  @Test
  void validation_shouldPass() {
    var request = buildValid()
        .callbackUri("https://example.com")
        .build();
    assertThatValidationPasses(request);
  }

  @ParameterizedTest
  @MethodSource
  void validation_shouldFail(Set<String> invalidProps, DiagnosticRequest request) {
    assertThatValidationFails(invalidProps, request);
  }

  private static Stream<Arguments> validation_shouldFail() { // test data
    return Stream.of(
        Arguments.of(
            Set.of("orgId", "calendarId"),
            ModelBuilders.diagnosticRequest()
                .build()), // everything is null
        Arguments.of(
            Set.of("orgId.value", "calendarId.value"),
            buildValid()
                .orgId(new OrgId(" "))
                .calendarId(new CalendarId(" "))
                .build()),
        Arguments.of(
            Set.of("callbackUri"),
            buildValid()
                .callbackUri("ftp://invalid")
                .build()));
  }

  private static ModelBuilders.DiagnosticRequestBuilder buildValid() {
    return ModelBuilders.diagnosticRequest()
        .orgId(TestData.orgId())
        .calendarId(CalendarId.create());
  }
}
