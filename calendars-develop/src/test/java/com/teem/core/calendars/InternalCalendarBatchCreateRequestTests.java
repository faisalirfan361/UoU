package com.UoU.core.calendars;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationPasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.UoU._helpers.ModelBuilders;
import java.util.Set;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class InternalCalendarBatchCreateRequestTests {

  @Test
  void validation_shouldPass() {
    assertThatValidationPasses(buildValid().build());
    assertThatValidationPasses(buildValid().increment(-1).start(0).end(-1).build());
  }

  @ParameterizedTest
  @MethodSource
  void validation_shouldFail(Set<String> invalidProps, InternalCalendarBatchCreateRequest request) {
    assertThatValidationFails(invalidProps, request);
  }

  private static Stream<Arguments> validation_shouldFail() { // test data
    return Stream.of(
        Arguments.of(
            Set.of("orgId", "namePattern", "timezone", "start", "end", "increment"),
            ModelBuilders.internalCalendarBatchCreateRequest().build()), // everything is null
        Arguments.of(
            Set.of("namePattern"),
            buildValid().namePattern("missing number token").build()),
        Arguments.of(
            Set.of("timezone"),
            buildValid().timezone("invalid").build()),
        Arguments.of(
            Set.of("end"),
            buildValid().increment(1).start(2).end(1).build()),
        Arguments.of(
            Set.of("end"),
            buildValid().increment(-1).start(1).end(2).build()));
  }

  @Test
  void formatName_shouldReplaceNumber() {
    val request = buildValid().namePattern("test {n} name").build();
    val result = request.formatName(1);
    assertThat(result).isEqualTo("test 1 name");
  }

  @Test
  void formatName_shouldIndicateDryRun() {
    val request = buildValid()
        .namePattern("{n} Test")
        .isDryRun(true)
        .build();
    val result = request.formatName(1);
    assertThat(result).isEqualTo("1 Test (DRY RUN)");
  }

  private static ModelBuilders.InternalCalendarBatchCreateRequestBuilder buildValid() {
    return ModelBuilders.internalCalendarBatchCreateRequestWithTestData();
  }
}
