package com.UoU.core.calendars;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationPasses;

import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU.core.DataConfig;
import com.UoU.core.TimeSpan;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AvailabilityRequestTests {
  private static final Instant NOW = Instant.now();

  @Test
  void validation_shouldPass() {
    assertThatValidationPasses(buildValid().build());
  }

  @ParameterizedTest
  @MethodSource
  void validation_shouldFail(Set<String> invalidProps, AvailabilityRequest request) {
    assertThatValidationFails(invalidProps, request);
  }

  private static Stream<Arguments> validation_shouldFail() { // test data

    return Stream.of(
        Arguments.of(
            Set.of("orgId", "calendarIds", "timeSpan"),
            new AvailabilityRequest(null, null, null)),
        Arguments.of(
            Set.of("calendarIds"),
            buildValid().calendarIds(Set.of()).build()),
        Arguments.of(
            Set.of("calendarIds"),
            buildValid()
                .calendarIds(Stream
                    .generate(CalendarId::create)
                    .limit(DataConfig.Availability.MAX_CALENDARS + 1)
                    .collect(Collectors.toSet()))
                .build()),
        Arguments.of(
            Set.of("timeSpan.end"),
            buildValid()
                .timeSpan(new TimeSpan(
                    NOW,
                    NOW.plus(DataConfig.Availability.MAX_DURATION).plusSeconds(1)))
                .build())
    );
  }

  private static ModelBuilders.AvailabilityRequestBuilder buildValid() {
    return ModelBuilders.availabilityRequest()
        .orgId(TestData.orgId())
        .calendarIds(Stream
            .generate(CalendarId::create)
            .limit(DataConfig.Availability.MAX_CALENDARS)
            .collect(Collectors.toSet()))
        .timeSpan(new TimeSpan(NOW, NOW.plus(DataConfig.Availability.MAX_DURATION)));
  }
}
