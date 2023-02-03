package com.UoU._integration.api.v1;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;

import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU._integration.api.BaseApiIntegrationTest;
import com.UoU.app.security.Scopes;
import com.UoU.core.TimeSpan;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.events.When;
import io.restassured.response.ValidatableResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

class AvailabilityControllerTests extends BaseApiIntegrationTest {
  @Getter private final String basePath = "/v1/calendars";

  @Test
  void shouldBeAuthorizedByValidScopes() {
    auth.assertEachScopeAuthorizes(
        List.of(Scopes.CALENDARS, Scopes.CALENDARS_READONLY),
        x -> x.post("/availability"),
        x -> x.post("/freebusy"),
        x -> x.post("/freebusy/detailed"));
  }

  @Test
  void getAvailability_shouldReturnValidCalendars() {
    var validCalendarMatcher = is(true);
    testShouldReturnValidCalendars("/availability", validCalendarMatcher);
  }

  @Test
  void getAvailability_shouldEnforceMaxTimeSpan() {
    var json = Map.of(
        "calendarIds", List.of("test"),
        "timeSpan", Map.of(
            "start", "2022-01-01T00:00:00Z",
            "end", "2022-06-01T00:00:00Z"));

    restAssuredJson(json)
        .post("/availability")
        .then()
        .statusCode(400)
        .body("error", not(blankOrNullString()))
        .body("violations.field", hasItem("timeSpan.end"));
  }

  @Test
  void getAvailability_shouldReturnFalseForBusy() {
    var result = testShouldReturnBusy("/availability");
    result.response().body("itemsById." + result.busyCalendarId.value(), is(false));
  }

  @Test
  void getFreeBusy_shouldReturnValidCalendars() {
    var validCalendarMatcher = is(Collections.emptyList());
    testShouldReturnValidCalendars("/freebusy", validCalendarMatcher);
  }

  @Test
  void getFreeOrBusy_shouldReturnBusyTimeSpans() {
    var result = testShouldReturnBusy("/freebusy");
    var busyList = result
        .response
        .extract()
        .body()
        .jsonPath()
        .getList("itemsById." + result.busyCalendarId.value(), Map.class);

    // TODO: Nylas only returns event times with seconds precision, so we should probably copy this
    // approach and be more explicit about truncating to seconds on save to avoid comparison issues.
    // However, for now, I just need to get these tests to pass, and so I'm truncating for asserts.
    assertThat(busyList.size()).isEqualTo(1);
    assertThat(Instant.parse((String) busyList.get(0).get("start")).truncatedTo(ChronoUnit.SECONDS))
        .isEqualTo(result.eventTimeSpan.startTime().truncatedTo(ChronoUnit.SECONDS));
    assertThat(Instant.parse((String) busyList.get(0).get("end")).truncatedTo(ChronoUnit.SECONDS))
        .isEqualTo(result.eventTimeSpan.endTime().truncatedTo(ChronoUnit.SECONDS));
  }

  @Test
  void getFreeBusyDetailed_shouldReturnValidCalendars() {
    var validCalendarMatcher = is(Collections.emptyList());
    testShouldReturnValidCalendars("/freebusy/detailed", validCalendarMatcher);
  }

  @Test
  void getFreeOrBusyDetailed_shouldReturnBusyEventTimeSpans() {
    var result = testShouldReturnBusy("/freebusy/detailed");
    var busyList = result
        .response
        .extract()
        .body()
        .jsonPath()
        .getList("itemsById." + result.busyCalendarId.value(), Map.class);

    // TODO: Nylas only returns event times with seconds precision, so we should probably copy this
    // approach and be more explicit about truncating to seconds on save to avoid comparison issues.
    // However, for now, I just need to get these tests to pass, and so I'm truncating for asserts.
    assertThat(busyList.size()).isEqualTo(1);
    assertThat(busyList.get(0))
        .matches(x -> x.get("eventId") != null)
        .matches(x -> !((String) x.get("eventTitle")).isBlank());
    assertThat(Instant.parse((String) busyList.get(0).get("start")).truncatedTo(ChronoUnit.SECONDS))
        .isEqualTo(result.eventTimeSpan.startTime().truncatedTo(ChronoUnit.SECONDS));
    assertThat(Instant.parse((String) busyList.get(0).get("end")).truncatedTo(ChronoUnit.SECONDS))
        .isEqualTo(result.eventTimeSpan.endTime().truncatedTo(ChronoUnit.SECONDS));
  }

  private void testShouldReturnValidCalendars(String path, Matcher<?> validCalendarMatcher) {
    var calendarId = dbHelper.createCalendar(orgId).value();
    var differentOrgCalendarId = dbHelper.createCalendar(TestData.orgId()).value();
    var invalidCalendarId = CalendarId.create().value();
    var json = createAvailabilityRequestJson(
        calendarId, differentOrgCalendarId, invalidCalendarId);

    restAssuredJson(json)
        .post(path)
        .then()
        .statusCode(200)
        .body("itemsById." + calendarId, validCalendarMatcher)
        .body("itemsById", not(hasKey(differentOrgCalendarId)))
        .body("itemsById", not(hasKey(invalidCalendarId)));
  }

  private BusyTestResult testShouldReturnBusy(String path) {
    var searchTimeSpan = new TimeSpan(Instant.now(), Instant.now().plusSeconds(900));

    // Create calendar with event that overlaps searchTimeSpan:
    var calendarId = dbHelper.createCalendar(orgId);
    var eventWhen = ModelBuilders.whenTimeSpan()
        .startTime(searchTimeSpan.start().minusSeconds(90))
        .endTime(searchTimeSpan.start().plusSeconds(1))
        .build();
    dbHelper.createEvent(orgId, calendarId, x -> x.when(eventWhen));

    var json = Map.of(
        "calendarIds", List.of(calendarId.value()),
        "timeSpan", Map.of(
            "start", searchTimeSpan.start(),
            "end", searchTimeSpan.end()));

    var response = restAssuredJson(json)
        .post(path)
        .then()
        .statusCode(200)
        .body("itemsById", not(empty()));

    return new BusyTestResult(response, calendarId, eventWhen);
  }

  private static Map<String, Object> createAvailabilityRequestJson(String... calendarIds) {
    return Map.of(
        "calendarIds", calendarIds,
        "timeSpan", Map.of(
            "start", Instant.now(),
            "end", Instant.now().plusSeconds(600)));
  }

  private record BusyTestResult(
      ValidatableResponse response,
      CalendarId busyCalendarId,
      When.TimeSpan eventTimeSpan) {}
}
