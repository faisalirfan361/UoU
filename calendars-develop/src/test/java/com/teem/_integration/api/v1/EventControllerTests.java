package com.UoU._integration.api.v1;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.oneOf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.UoU._fakes.FakeGraphServiceClient;
import com.UoU._helpers.TestData;
import com.UoU._integration.api.BaseApiIntegrationTest;
import com.UoU.app.security.Scopes;
import com.UoU.core.Fluent;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.events.Event;
import com.UoU.core.events.EventConstraints;
import com.UoU.core.events.EventId;
import com.UoU.core.events.ParticipantRequest;
import com.UoU.core.events.ParticipantStatus;
import com.UoU.core.events.Recurrence;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class EventControllerTests extends BaseApiIntegrationTest {
  @Getter private final String basePath = "/v1/events";

  private static MockWebServer MOCK_SERVER;

  @BeforeAll
  static void beforeAll() throws IOException {
    MOCK_SERVER = new MockWebServer();
    MOCK_SERVER.start();
  }

  @AfterAll
  static void afterAll() throws IOException {
    MOCK_SERVER.shutdown();
  }

  @Test
  void reads_shouldBeAuthorizedByValidScopes() {
    auth.assertEachScopeAuthorizes(
        List.of(Scopes.EVENTS, Scopes.EVENTS_READONLY),
        x -> x.get("bycalendar/{calendarId}", TestData.uuidString()), // list by calendar
        x -> x.get("/{id}", TestData.uuidString()));
  }

  @Test
  void writes_shouldBeAuthorizedByValidScopes() {
    auth.assertScopeAuthorizes(
        Scopes.EVENTS,
        x -> x.post(), // create
        x -> x.put(TestData.uuidString()), // update by id
        x -> x.delete(TestData.uuidString()),  // delete by id
        x -> x.post("/{id}/checkin", TestData.uuidString()),
        x -> x.post("/{id}/checkout", TestData.uuidString()));
  }

  @Test
  void listByCalendar_shouldWork() {
    val calendarId = dbHelper.createCalendar(orgId);
    val eventIds = dbHelper.createEvents(orgId, calendarId, 3)
        .limit(2)
        .map(x -> x.value().toString())
        .toArray(String[]::new);

    // Create an event in a different org to make sure it's filtered out.
    // This shouldn't happen in real life, but we want to ensure the event.org_id is checked.
    dbHelper.createEvent(TestData.orgId(), calendarId);

    // Page 1:
    val nextCursor = restAssured()
        .queryParam("limit", 1)
        .get("/bycalendar/{calendarId}", calendarId.value())
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("items.size()", is(1))
        .body("items.id", everyItem(oneOf(eventIds)))
        .body("items.calendarId", everyItem(is(calendarId.value())))
        .body("items.participants.findAll().sum().size()", is(3))
        .body("items", everyItem(hasKey("checkinAt")))
        .body("items", everyItem(hasKey("checkoutAt")))
        .body("meta.nextCursor", not(blankOrNullString()))
        .extract().jsonPath().getString("meta.nextCursor");

    // Page 2:
    restAssured()
        .queryParam("cursor", nextCursor)
        .get("/bycalendar/{calendarId}", calendarId.value())
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("items.size()", is(1))
        .body("items.id", everyItem(oneOf(eventIds)))
        .body("meta.nextCursor", nullValue());
  }

  @Test
  void listByCalendar_shouldExpandRecurring() {
    val calendarId = dbHelper.createCalendar(orgId);
    val masterId = EventId.create(); // recurrence series master id
    dbHelper.createEvents(
            orgId, calendarId,
            x -> x.recurrence(Recurrence.none()),
            x -> x.id(masterId).recurrence(TestData.recurrenceMaster()),
            x -> x.recurrence(Recurrence.instance(masterId, false)), // non-override
            x -> x.recurrence(Recurrence.instance(masterId, false)), // non-override
            x -> x.recurrence(Recurrence.instance(masterId, true))) // override
        .toList();

    // Each item may have "recurrence" for the master or "recurrenceInstance" but not both.
    val isMaster = allOf(
        hasEntry("id", masterId.value().toString()),
        hasKey("recurrence"),
        not(hasKey("recurrenceInstance")));
    val isInstance = allOf(
        not(hasKey("recurrence")),
        hasKey("recurrenceInstance"));
    val isNonRecurring = allOf(
        not(hasKey("recurrence")),
        not(hasKey("recurrenceInstance")));

    // Default of expandRecurring=false:
    // Override instances are always returned, so we'll expect 1 master, 1 override instance.
    restAssured()
        .get("/bycalendar/{calendarId}", calendarId.value())
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("items", containsInAnyOrder(isMaster, isInstance, isNonRecurring));

    // Expand recurring:
    // Return all 3 instances, not master.
    restAssured()
        .queryParam("expandRecurring", true)
        .get("/bycalendar/{calendarId}", calendarId.value())
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("items", containsInAnyOrder(isInstance, isInstance, isInstance, isNonRecurring));
  }

  @Test
  void listByCalendar_shouldReturnDebugInfoWhenRequested() {
    val calendarId = dbHelper.createCalendar(orgId);
    dbHelper.createEvent(orgId, calendarId);

    restAssured()
        .get("/bycalendar/{calendarId}", calendarId.value())
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("items", everyItem(not(hasKey("debugInfo"))));

    restAssured()
        .queryParam("includeDebugInfo", true)
        .get("/bycalendar/{calendarId}", calendarId.value())
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("items", everyItem(hasKey("debugInfo")));
  }

  @Test
  void listByCalendar_shouldFilterByWhen() {
    val calendarId = dbHelper.createCalendar(orgId);
    val when = TestData.whenTimeSpan();
    val id = dbHelper.createEvent(orgId, calendarId, x -> x.when(when));

    // Use all when filters, and find single event.
    restAssured()
        .queryParam("startsAfter", when.startTime().minusSeconds(1).toString())
        .queryParam("startsBefore", when.startTime().plusSeconds(1).toString())
        .queryParam("endsAfter", when.endTime().minusSeconds(1).toString())
        .queryParam("endsBefore", when.endTime().plusSeconds(1).toString())
        .get("/bycalendar/{calendarId}", calendarId.value())
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("items.size()", is(1))
        .body("items.id", everyItem(is(id.value().toString())));

    // Make sure filters exclude event when non-matching.
    restAssured()
        .queryParam("startsAfter", when.startTime().plusSeconds(1).toString())
        .get("/bycalendar/{calendarId}", calendarId.value())
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("items.size()", is(0));
  }

  @Test
  void create_shouldReturnId() {
    val calendarId = dbHelper.createCalendar(orgId);
    val json = getCommonEventJson(calendarId);

    restAssuredJson(json)
        .post()
        .then()
        .statusCode(HttpStatus.SC_CREATED)
        .body("id", not(blankOrNullString()));
  }

  @Test
  void create_shouldHandleRecurrrence() {
    val calendarId = dbHelper.createCalendar(orgId);
    val json = getCommonEventJson(calendarId);
    val rrule = "RRULE:FREQ=DAILY;UNTIL=20990201T120000Z";
    json.put("recurrence", Map.of("rrule", List.of(rrule), "timezone", "America/Chicago"));

    val id = restAssuredJson(json)
        .post()
        .then()
        .statusCode(HttpStatus.SC_CREATED)
        .body("id", not(blankOrNullString()))
        .extract().body().jsonPath().getUUID("id");

    assertThat(dbHelper.getEvent(new EventId(id)).getRecurrence().data()).contains(rrule);
  }

  @Test
  @SneakyThrows
  void create_shouldAddTeamsConferencing(
      @Autowired FakeGraphServiceClient fakeGraphServiceClient,
      @Autowired ObjectMapper objectMapper) {

    // Have the mock server return a fake meeting when a request is made.
    fakeGraphServiceClient.setServiceRoot(MOCK_SERVER.url("").toString());
    val fakeMeeting = TestData.teamsMeeting();
    MOCK_SERVER.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(objectMapper.writeValueAsString(fakeMeeting)));

    val principalEmail = TestData.email();
    val calendarId = dbHelper.createCalendar(orgId);
    val userId = dbHelper.createConferencingUser(orgId, x -> x
        .email(principalEmail)); // principal and conferencing user email must match
    val json = getCommonEventJson(calendarId);
    val confJson = Map.of("autoCreate", Map.of("userId", userId.value(), "language", "fr-CA"));
    json.put("conferencing", confJson);

    val id = restAssuredJson(json)
        .auth().oauth2(
            auth.createJwt(
                auth.buildClaimsWithFullAccess().subject(principalEmail).build()))
        .post()
        .then()
        .statusCode(HttpStatus.SC_CREATED)
        .body("id", not(blankOrNullString()))
        .extract().body().jsonPath().getUUID("id");

    assertThat(MOCK_SERVER.takeRequest(0, TimeUnit.SECONDS)).isNotNull();
    assertThat(dbHelper.getEvent(new EventId(id)).getDescription())
        .contains("Microsoft Teams meeting");
  }

  @Test
  @SneakyThrows
  void create_shouldReturn400WhenConferencingUserEmailDoesNotMatchPrincipalEmail() {
    val calendarId = dbHelper.createCalendar(orgId);
    val userId = dbHelper.createConferencingUser(orgId, x -> x
        .email(TestData.email())); // some random email
    val json = getCommonEventJson(calendarId);
    val confJson = Map.of("autoCreate", Map.of("userId", userId.value(), "language", "fr-CA"));
    json.put("conferencing", confJson);

    restAssuredJson(json)
        .post()
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body("error", not(blankOrNullString()))
        .body("violations.field", hasItem("conferencing.autoCreate.userId"));
  }

  @Test
  void create_shouldReturn400WhenInvalidJson() {
    val json = "{\"invalid\": true}";

    restAssuredJson(json)
        .post()
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body("error", not(blankOrNullString()))
        .body("violations.field", hasItems("calendarId", "when")); // check a sample of fields
  }

  @Test
  void create_shouldReturn400ForInvalidCalendarId() {
    val invalidIdJson = getCommonEventJson(
        CalendarId.create());
    val wrongOrgJson = getCommonEventJson(
        dbHelper.createCalendar(TestData.orgId()));
    val readOnlyJson = getCommonEventJson(
        dbHelper.createCalendar(orgId, x -> x.isReadOnly(true)));

    Stream.of(invalidIdJson, wrongOrgJson, readOnlyJson).forEach(json ->
        restAssuredJson(json)
            .post()
            .then()
            .statusCode(HttpStatus.SC_BAD_REQUEST)
            .body("error", not(blankOrNullString()))
            .body("violations.field", hasItems("calendarId")));
  }

  @Test
  void create_shouldReturn400ForTitleTooLong() {
    val json = getCommonEventJson(CalendarId.create());
    json.put("title", "x".repeat(EventConstraints.TITLE_MAX) + 1);

    restAssuredJson(json)
        .post()
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body("error", not(blankOrNullString()))
        .body("violations.field", hasItems("title"));
  }

  @Test
  void update_shouldReturnNoContent() {
    val calendarId = dbHelper.createCalendar(orgId);
    val eventId = dbHelper.createEvent(orgId, calendarId);
    val json = getCommonEventJson();

    val newTitle = TestData.uuidString();
    json.put("title", newTitle);

    restAssuredJson(json)
        .put(eventId.value().toString())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // Check that update worked:
    assertThat(dbHelper.getEvent(eventId).getTitle()).isEqualTo(newTitle);
  }

  @Test
  void update_shouldReturn400WhenInvalidJson() {
    val calendarId = dbHelper.createCalendar(orgId);
    val eventId = dbHelper.createEvent(orgId, calendarId);
    val json = "{\"invalid\": true}";

    restAssuredJson(json)
        .put(eventId.value().toString())
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body("error", not(blankOrNullString()))
        .body("violations.field", hasItems("when"));
  }

  @Test
  void update_shouldReturn400ForLocationTooLong() {
    val json = getCommonEventJson(CalendarId.create());
    json.put("location", "x".repeat(EventConstraints.LOCATION_MAX) + 1);

    restAssuredJson(json)
        .post()
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body("error", not(blankOrNullString()))
        .body("violations.field", hasItems("location"));
  }

  @Test
  void update_shouldReturn400ForReadOnlyEvent() {
    val calendarId = dbHelper.createCalendar(orgId);
    val eventId = dbHelper.createEvent(orgId, calendarId, x -> x.isReadOnly(true));
    val json = getCommonEventJson();

    restAssuredJson(json)
        .put(eventId.value().toString())
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body("error", containsString("read-only"));
  }

  @Test
  void update_shouldReturn404ForInvalidEventId() {
    val invalidId = EventId.create().value().toString();
    val wrongOrgId = Fluent
        .of(TestData.orgId())
        .map(x -> dbHelper.createEvent(x, dbHelper.createCalendar(x)).value().toString())
        .get();

    Stream.of(invalidId, wrongOrgId).forEach(id ->
        restAssuredJson(getCommonEventJson())
            .put(id)
            .then()
            .statusCode(HttpStatus.SC_NOT_FOUND));
  }

  @Test
  void update_shouldKeepExistingFieldsThatDontChangeThroughApi() {
    // Fields that don't change through api:
    val externalId = TestData.eventExternalId();
    val icalUid = TestData.uuidString();
    val status = Event.Status.CONFIRMED;
    val isReadOnly = false; // must be false for api to allow updates
    val participant = ParticipantRequest.builder()
        .name(TestData.uuidString())
        .email(TestData.email())
        .status(ParticipantStatus.MAYBE)
        .comment(TestData.uuidString())
        .build();

    val eventId = dbHelper.createEvent(
        orgId,
        dbHelper.createCalendar(orgId),
        x -> x
            .externalId(externalId)
            .icalUid(icalUid)
            .status(status)
            .isReadOnly(isReadOnly)
            .participants(List.of(participant)));
    val json = getCommonEventJson();

    val newTitle = TestData.uuidString();
    json.put("title", newTitle);

    val newParticipantName = TestData.uuidString();
    json.put("participants", List.of(Map.of(
        "name", newParticipantName,
        "email", participant.email())));

    restAssuredJson(json)
        .put(eventId.value().toString())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // Check that update worked and kept fields that don't change through api:
    val updatedEvent = dbHelper.getEvent(eventId);
    assertThat(updatedEvent.getTitle()).isEqualTo(newTitle);
    assertThat(updatedEvent.getExternalId()).isEqualTo(externalId.value());
    assertThat(updatedEvent.getIcalUid()).isEqualTo(icalUid);
    assertThat(Event.Status.byStringValue(updatedEvent.getStatus().getLiteral())).isEqualTo(status);
    assertThat(updatedEvent.getIsReadOnly()).isEqualTo(isReadOnly);

    val updatedParticipants = dbHelper.getParticipants(eventId);
    assertThat(updatedParticipants.size()).isEqualTo(1);
    assertThat(updatedParticipants.get(0).getName()).isEqualTo(newParticipantName);
    assertThat(updatedParticipants.get(0).getStatus().getLiteral())
        .isEqualTo(participant.status().toString().toLowerCase(Locale.ROOT));
    assertThat(updatedParticipants.get(0).getComment()).isEqualTo(participant.comment());
  }

  @Test
  void create_update_get_shouldHandleDataSource() {
    val calendarId = dbHelper.createCalendar(orgId);
    val createDataSource = TestData.uuidString();
    val updateDataSource = TestData.uuidString();

    // Create with dataSource
    val createJson = getCommonEventJson(calendarId);
    createJson.put("dataSource", createDataSource);
    val id = restAssuredJson(createJson)
        .post()
        .then()
        .statusCode(HttpStatus.SC_CREATED)
        .extract().body().jsonPath().getUUID("id");

    // Update with dataSource
    val updateJson = getCommonEventJson(calendarId);
    updateJson.put("location", "changed-because-we-must-change-one-real-field");
    updateJson.put("dataSource", updateDataSource);
    restAssuredJson(updateJson)
        .put(id.toString())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // GET event by id
    val results = Fluent
        .of(restAssured()
            .get(id.toString())
            .then()
            .statusCode(HttpStatus.SC_OK)
            .extract().body().jsonPath())
        .map(x -> Pair.of(x.getString("createdFrom"), x.getString("updatedFrom")))
        .get();

    // Verify that both dataSources were stored, allowing for "api-" prefix.
    assertThat(results.getLeft()).endsWith(createDataSource);
    assertThat(results.getRight()).endsWith(updateDataSource);
  }

  @Test
  void delete_shouldReturnNoContent() {
    val calendarId = dbHelper.createCalendar(orgId);
    val eventId = dbHelper.createEvent(orgId, calendarId);

    restAssured()
        .delete(eventId.value().toString())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // Check that delete worked:
    restAssured()
        .delete(eventId.value().toString())
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);
  }

  @Test
  void delete_shouldReturn404() {
    restAssured()
        .delete(TestData.uuidString())
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);
  }

  @Test
  void get_shouldWork() {
    val calendarId = dbHelper.createCalendar(orgId);
    val eventId = dbHelper.createEvent(orgId, calendarId);

    restAssured()
        .get("/{id}", eventId.value())
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", not(blankOrNullString()))
        .body("calendarId", not(blankOrNullString()))
        .body("title", not(blankOrNullString()))
        .body("location", not(blankOrNullString()))
        .body("when", not(blankOrNullString()))
        .body("", hasKey("participants")); // Check a subset of fields
  }

  @Test
  void get_shouldReturnDebugInfoWhenRequested() {
    val calendarId = dbHelper.createCalendar(orgId);
    val eventId = dbHelper.createEvent(orgId, calendarId);

    restAssured()
        .get("/{id}", eventId.value())
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", not(blankOrNullString()))
        .body(".", not(hasKey("debugInfo")));

    restAssured()
        .queryParam("includeDebugInfo", true)
        .get("/{id}", eventId.value())
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", not(blankOrNullString()))
        .body(".", hasKey("debugInfo"));
  }

  @Test
  void get_shouldReturnNotFound() {
    restAssured()
        .get("/{id}", TestData.uuidString())
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);
  }

  @Test
  void checkinAndCheckout_shouldWork() {
    val calendarId = dbHelper.createCalendar(orgId);
    val eventId = dbHelper.createEvent(orgId, calendarId);

    restAssured()
        .post("/{id}/checkin", eventId.value())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    restAssured()
        .post("{id}/checkout", eventId.value())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    restAssured()
        .get("/{id}", eventId.value())
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("checkinAt", is(notNullValue()))
        .body("checkoutAt", is(notNullValue()));
  }

  @Test
  void checkinAndCheckout_shouldReturnNotFound() {
    restAssured()
        .post("/{id}/checkin", TestData.uuidString())
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);

    restAssured()
        .post("/{id}/checkout", TestData.uuidString())
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);
  }

  @Test
  void checkinAndCheckout_shouldRecordDataSource() {
    val eventId = dbHelper.createEvent(orgId, dbHelper.createCalendar(orgId));
    val checkinDataSource = TestData.uuidString();
    val checkoutDataSource = TestData.uuidString();

    // Checkin
    restAssured()
        .queryParam("dataSource", checkinDataSource)
        .post("/{id}/checkin", eventId.value())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    assertThat(dbHelper.getEvent(eventId).getUpdatedFrom()).endsWith(checkinDataSource);

    // Checkout
    restAssured()
        .queryParam("dataSource", checkoutDataSource)
        .post("/{id}/checkout", eventId.value())
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    assertThat(dbHelper.getEvent(eventId).getUpdatedFrom()).endsWith(checkoutDataSource);
  }

  private static Map<String, Object> getCommonEventJson() {
    return new HashMap<>(Map.of(
        "title", TestData.uuidString(),
        "description", "description",
        "location", "location",
        "when", Map.of(
            "type", "timespan",
            "startTime", Instant.now().truncatedTo(ChronoUnit.MINUTES).toString(),
            "endTime", Instant.now().plusSeconds(900).toString()
        ),
        "participants", List.of(
            Map.of(
                "name", "Test Participant",
                "email", "test@test.com"
            )
        )
    ));
  }

  private static Map<String, Object> getCommonEventJson(CalendarId calendarId) {
    val json = getCommonEventJson();
    json.put("calendarId", calendarId.value());
    return json;
  }
}
