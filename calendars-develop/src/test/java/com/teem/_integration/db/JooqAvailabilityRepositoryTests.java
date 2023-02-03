package com.UoU._integration.db;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.UoU._helpers.ModelBuilders;
import com.UoU._integration.BaseAppIntegrationTest;
import com.UoU.core.TimeSpan;
import com.UoU.core.calendars.AvailabilityRequest;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.events.When;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class JooqAvailabilityRepositoryTests extends BaseAppIntegrationTest {

  /**
   * Tests that all the availability methods return the correct availability for various scenarios.
   *
   * <p>The availability logic for all methods works the same, but then the specific data that's
   * returned based on available/unavailable is different per method.
   */
  @ParameterizedTest
  @MethodSource
  void allAvailabilityMethods_shouldReturnCorrectAvailability(
      boolean expectedAvailability,
      String scenarioDescription,
      TimeSpan searchWindow,
      String calendarTimezone,
      When eventWhen) {

    // Setup the scenario with a calendar and event.
    // Also create one event outside the searchWindow to make things more realistic.
    val calendarId = dbHelper.createCalendar(orgId, x -> x.timezone(calendarTimezone));
    dbHelper.createEvent(orgId, calendarId, x -> x.when(eventWhen));
    dbHelper.createEvent(orgId, calendarId, x -> x.when(whenTimeSpan(
        searchWindow.end(),
        searchWindow.end().plusSeconds(60))));
    val request = new AvailabilityRequest(
        orgId, Set.of(calendarId, new CalendarId("invalid")), searchWindow);

    // Create some helpers for later assertions.
    val resultSizeDescription = scenarioDescription + " - Result should have one calendar.";
    val resultDescription = scenarioDescription + " - Calendar availability must be "
        + (expectedAvailability ? "AVAILABLE" : "UNAVAILABLE") + ".";
    val resultBusyPeriodDescription = scenarioDescription
        + " - Busy period should match event timespan as interpreted by toUtcTimeSpan().";
    val expectedBusyTimeSpan = eventWhen.toUtcTimeSpan(() -> ZoneId.of(calendarTimezone));

    // Call getAvailability and check result.
    val availabilityResult = dbHelper.getAvailabilityRepo().getAvailability(request);
    assertThat(availabilityResult.keySet().size())
        .as("getAvailability - " + resultSizeDescription)
        .isEqualTo(1);
    assertThat(availabilityResult.get(calendarId))
        .as("getAvailability - " + resultDescription)
        .isEqualTo(expectedAvailability);

    // Call getBusyPeriods and check result.
    val busyResult = dbHelper.getAvailabilityRepo().getBusyPeriods(request);
    assertThat(busyResult.keySet().size())
        .as("getBusyPeriods - " + resultSizeDescription)
        .isEqualTo(1);
    assertThat(busyResult.get(calendarId).size())
        .as("getBusyPeriods - " + resultDescription)
        .isEqualTo(expectedAvailability ? 0 : 1);
    if (!expectedAvailability) {
      assertThat(busyResult.get(calendarId).get(0))
          .as("getBusyPeriods - " + resultBusyPeriodDescription)
          .isEqualTo(expectedBusyTimeSpan);
    }

    // Call get DetailedBusyPeriods and check result.
    val busyDetailedResult = dbHelper.getAvailabilityRepo().getDetailedBusyPeriods(request);
    assertThat(busyDetailedResult.keySet().size())
        .as("getDetailedBusyPeriods - " + resultSizeDescription)
        .isEqualTo(1);
    assertThat(busyDetailedResult.get(calendarId).size())
        .as("getDetailedBusyPeriods - " + resultDescription)
        .isEqualTo(expectedAvailability ? 0 : 1);
    if (!expectedAvailability) {
      assertThat(busyDetailedResult.get(calendarId).get(0).timeSpan())
          .as("getDetailedBusyPeriods - " + resultBusyPeriodDescription)
          .isEqualTo(expectedBusyTimeSpan);
    }
  }

  private static Stream<Arguments> allAvailabilityMethods_shouldReturnCorrectAvailability() {
    val start = Instant.parse("2022-02-05T00:00:00Z");
    val end = Instant.parse("2022-02-06T00:00:00Z");
    val searchWindow = new TimeSpan(start, end);

    // Note: for timeSpans, calendar timezones don't matter because we already have exact times.
    return Stream.of(
        // events that make calendar unavailable:
        Arguments.of(
            false,
            "Event inside window",
            searchWindow,
            "UTC",
            whenTimeSpan(start.plus(30, MINUTES), start.plus(45, MINUTES))),
        Arguments.of(
            false,
            "Event aligns with start of window",
            searchWindow,
            "America/Denver",
            whenTimeSpan(start, start.plus(60, MINUTES))),
        Arguments.of(
            false,
            "Event aligns with end of window",
            searchWindow,
            "America/Chicago",
            whenTimeSpan(start.plus(60, MINUTES), end)),
        Arguments.of(
            false,
            "Event overlaps start of window",
            searchWindow,
            "UTC",
            whenTimeSpan(start.minus(5, MINUTES), start.plus(5, MINUTES))),
        Arguments.of(
            false,
            "Event overlaps end of window",
            searchWindow,
            "UTC",
            whenTimeSpan(end.minus(5, MINUTES), end.plus(5, MINUTES))),

        // Events that keep calendar available
        Arguments.of(
            true,
            "Event abuts start of window",
            searchWindow,
            "UTC",
            whenTimeSpan(start.minus(5, MINUTES), start)),
        Arguments.of(
            true,
            "Event abuts end of window",
            searchWindow,
            "UTC",
            whenTimeSpan(end, end.plus(5, MINUTES))),

        // all days events and timezone handling
        Arguments.of(
            false,
            "All day event inside window (datespan)",
            timeSpan("2022-02-05T00:00:00Z", "2222-03-05T00:00:00Z"),
            "America/Denver",
            whenDateSpan("2022-02-10", "2022-02-12")),
        Arguments.of(
            false,
            "All day event inside window (date)",
            timeSpan("2022-02-05T00:00:00Z", "2222-03-05T00:00:00Z"),
            "America/Chicago",
            whenDate("2022-02-10")),
        Arguments.of(
            false,
            "All day event one minute inside window because of offset",
            timeSpan("2022-02-05T06:59:00Z", "2022-02-05T08:00:00Z"),
            "America/Denver", // -7h offset
            whenDate("2022-02-04")), // becomes 2022-02-04T07:00:00Z to 2022-02-05T07:00:00Z
        Arguments.of(
            true,
            "All day event abuts window end because of offset",
            timeSpan("2022-02-05T07:00:00Z", "2022-02-05T08:00:00Z"),
            "America/Denver", // -7h offset
            whenDate("2022-02-04")), // becomes 2022-02-04T07:00:00Z to 2022-02-05T07:00:00Z
        Arguments.of(
            false,
            "All day event overlaps end because of America/Denver DST change from -7h to -6h",
            timeSpan("2022-03-14T05:59:00Z", "2022-03-14T06:00:00Z"),
            "America/Denver", //
            whenDate("2022-03-13")), // becomes 2022-03-13T07:00:00Z to 2022-03-14T06:00:00Z
        Arguments.of(
            true,
            "All day event abuts end because of America/Denver DST change from -7h to -6h",
            timeSpan("2022-03-14T06:00:00Z", "2022-03-15T06:00:00Z"),
            "America/Denver", //
            whenDate("2022-03-13")) // becomes 2022-03-13T07:00:00Z to 2022-03-14T06:00:00Z
    );
  }

  @Test
  void busyMethods_shouldHandleDuplicateAndOverlappingTimes() {
    val calendarId = dbHelper.createCalendar(orgId);
    val baseTime = Instant.now().truncatedTo(ChronoUnit.DAYS);
    val when = whenTimeSpan(baseTime, baseTime.plusSeconds(900));
    val overlappingWhen = whenTimeSpan(when.startTime(), when.endTime().plusSeconds(60));
    val request = new AvailabilityRequest(
        orgId,
        Set.of(calendarId),
        new TimeSpan(when.startTime().minusSeconds(1), when.endTime().plusSeconds(1)));

    // Create 3 events: one base, one duplicate time, and one that overlaps the base time:
    dbHelper.createEvent(orgId, calendarId, x -> x.when(when));
    dbHelper.createEvent(orgId, calendarId, x -> x.when(when));
    dbHelper.createEvent(orgId, calendarId, x -> x.when(overlappingWhen));

    // getBusyPeriods() should exclude the duplicate but include the overlapping time as is.
    // The duplicate timespan is useless here because we only return the timespans.
    val busyResult = dbHelper.getAvailabilityRepo().getBusyPeriods(request);
    assertThat(busyResult.keySet()).containsExactly(calendarId);
    assertThat(busyResult.get(calendarId)).containsExactly(
        new TimeSpan(when.startTime(), when.endTime()),
        new TimeSpan(overlappingWhen.startTime(), overlappingWhen.endTime()));

    // getDetailedBusyPeriods() should include all the events, event the duplicate.
    // Because each result contains the event details, duplicates make sense here.
    val busyDetailedResult = dbHelper.getAvailabilityRepo().getDetailedBusyPeriods(request);
    assertThat(busyDetailedResult.keySet()).containsExactly(calendarId);
    assertThat(busyDetailedResult.get(calendarId).stream().map(x -> x.timeSpan())).containsExactly(
        new TimeSpan(when.startTime(), when.endTime()),
        new TimeSpan(when.startTime(), when.endTime()),
        new TimeSpan(overlappingWhen.startTime(), overlappingWhen.endTime()));
  }

  private static When.TimeSpan whenTimeSpan(Instant startTime, Instant endTime) {
    return ModelBuilders.whenTimeSpan().startTime(startTime).endTime(endTime).build();
  }

  private static When.DateSpan whenDateSpan(String startDate, String endDate) {
    return ModelBuilders.whenDateSpan()
        .startDate(LocalDate.parse(startDate))
        .endDate(LocalDate.parse(endDate))
        .build();
  }

  private static When.Date whenDate(String date) {
    return new When.Date(LocalDate.parse(date));
  }

  private static TimeSpan timeSpan(String startTime, String endTime) {
    return new TimeSpan(Instant.parse(startTime), Instant.parse(endTime));
  }
}
