package com.UoU.core.events;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationPasses;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class WhenTests {

  @ParameterizedTest
  @MethodSource
  void validation_shouldPass(When when) {
    assertThatValidationPasses(when);
  }

  private static Stream<When> validation_shouldPass() { // test data
    var instant = Instant.now();
    var localDate = LocalDate.now();

    return Stream.of(
        // TimeSpan
        ModelBuilders.whenTimeSpan()
            .startTime(instant)
            .endTime(instant.plusSeconds(1))
            .build(),
        ModelBuilders.whenTimeSpan()
            .startTime(instant)
            .endTime(instant.plusSeconds(1))
            .build(),
        ModelBuilders.whenTimeSpan()
            .startTime(instant)
            .endTime(instant.plusSeconds(1))
            .build(),

        // DateSpan
        ModelBuilders.whenDateSpan()
            .startDate(localDate)
            .endDate(localDate.plusDays(1))
            .build(),

        // Date
        new When.Date(localDate)
    );
  }

  @ParameterizedTest
  @MethodSource
  void validation_shouldFail(Set<String> invalidProps, When when) {
    assertThatValidationFails(invalidProps, when);
  }

  private static Stream<Arguments> validation_shouldFail() { // test data
    var instant = Instant.now();
    var localDate = LocalDate.now();

    return Stream.of(
        // TimeSpan
        Arguments.of(
            Set.of("startTime", "endTime"),
            ModelBuilders.whenTimeSpan()
                .build()), // everything null
        Arguments.of(
            Set.of("endTime"),
            ModelBuilders.whenTimeSpan()
                .startTime(instant)
                .endTime(instant) // not > start
                .build()),
        // DateSpan
        Arguments.of(
            Set.of("startDate", "endDate"),
            ModelBuilders.whenDateSpan()
                .build()), // everything null
        Arguments.of(
            Set.of("endDate"),
            ModelBuilders.whenDateSpan()
                .startDate(localDate)
                .endDate(localDate) // not > start
                .build()),

        // Date
        Arguments.of(
            Set.of("date"),
            new When.Date(null))
    );
  }

  @Test
  @SuppressWarnings("unchecked")
  void toUtcTimeSpan_shouldOnlyCallZoneSupplierForDateTypes() {
    Supplier<ZoneId> supplierMock = mock(Supplier.class);
    when(supplierMock.get()).thenReturn(ZoneId.of("America/Denver"));

    TestData.whenTimeSpan().toUtcTimeSpan(supplierMock);
    verifyNoInteractions(supplierMock);

    TestData.whenDateSpan().toUtcTimeSpan(supplierMock);
    verify(supplierMock, times(1)).get();

    TestData.whenDate().toUtcTimeSpan(supplierMock);
    verify(supplierMock, times(2)).get();
  }

  @Test
  void timeSpan_toUtcTimeSpan_shouldReturnSameTimes() {
    var when = TestData.whenTimeSpan();
    var utcTimeSpan = when.toUtcTimeSpan(() -> ZoneId.of("UTC"));

    assertThat(utcTimeSpan.start()).isEqualTo(when.startTime());
    assertThat(utcTimeSpan.end()).isEqualTo(when.endTime());
  }

  @Test
  void dateSpan_toUtcTimeSpan_shouldConvertWithTimeZone() {
    var zone = ZoneId.of("America/Denver");
    var when = new When.DateSpan(
        LocalDate.parse("2022-02-01"),
        LocalDate.parse("2022-02-05"));

    var utcTimeSpan = when.toUtcTimeSpan(() -> zone);

    assertThat(utcTimeSpan.start())
        .as("Start should have -7h America/Denver offset applied")
        .isEqualTo(Instant.parse("2022-02-01T07:00:00Z"));
    assertThat(utcTimeSpan.end())
        .as("End should should be +1 days to make date exclusive, and -7h offset applied")
        .isEqualTo(Instant.parse("2022-02-06T07:00:00Z"));
  }

  @Test
  void date_toUtcTimeSpan_shouldConvertWithTimeZone() {
    var zone = ZoneId.of("America/Denver");
    var when = new When.Date(LocalDate.parse("2022-02-01"));

    var utcTimeSpan = when.toUtcTimeSpan(() -> zone);

    assertThat(utcTimeSpan.start())
        .as("Start should have -7h America/Denver offset applied")
        .isEqualTo(Instant.parse("2022-02-01T07:00:00Z"));
    assertThat(utcTimeSpan.end())
        .as("End should should be +1 days to make date exclusive, and -7h offset applied")
        .isEqualTo(Instant.parse("2022-02-02T07:00:00Z"));
  }

  @Test
  void date_toUtcTimeSpan_shouldConvertTo23HourDayForSpringDaylightSavings() {
    // In America/Denver, 03-13 is a 23h day because you lose an hour at 2am for DST.
    var zone = ZoneId.of("America/Denver");
    var when = new When.Date(LocalDate.parse("2022-03-13")); // DST goes from -7 to -6 at 2am

    var utcTimeSpan = when.toUtcTimeSpan(() -> zone);

    assertThat(utcTimeSpan.start())
        .as("Start should have -7h America/Denver offset applied")
        .isEqualTo(Instant.parse("2022-03-13T07:00:00Z"));
    assertThat(utcTimeSpan.end())
        .as("End should should be +23h to account for new DST offset of -6h")
        .isEqualTo(Instant.parse("2022-03-14T06:00:00Z"));
    assertThat(utcTimeSpan.duration()).isEqualTo(Duration.ofHours(23));
  }

  @Test
  void date_toUtcTimeSpan_shouldConvertTo25HourDayForFallDaylightSavings() {
    // In America/Chicago, 11-06 is a 25h day because you gain an hour at 2am for DST.
    var zone = ZoneId.of("America/Chicago");
    var when = new When.Date(LocalDate.parse("2022-11-06")); // DST goes from -5 to -6 at 2am

    var utcTimeSpan = when.toUtcTimeSpan(() -> zone);

    assertThat(utcTimeSpan.start())
        .as("Start should have -5h America/Chicago offset applied")
        .isEqualTo(Instant.parse("2022-11-06T05:00:00Z"));
    assertThat(utcTimeSpan.end())
        .as("End should should be +25h to account for new DST offset of -6h")
        .isEqualTo(Instant.parse("2022-11-07T06:00:00Z"));
    assertThat(utcTimeSpan.duration()).isEqualTo(Duration.ofHours(25));
  }
}
