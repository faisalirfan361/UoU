package com.UoU._integration.api.v1;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.UoU._helpers.TestData;
import com.UoU._integration.api.BaseApiIntegrationTest;
import com.UoU.app.security.Scopes;
import com.UoU.core.Fluent;
import com.UoU.core.calendars.CalendarId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Test;

class DiagnosticControllerTests extends BaseApiIntegrationTest {
  @Getter private final String basePath = "/v1/diagnostics";

  @Test
  void all_shouldBeAuthorizedByValidScopes() {
    val calendarId = CalendarId.create().value();
    auth.assertEachScopeAuthorizes(
        List.of(Scopes.DIAGNOSTICS),
        x -> x.get("/sync/calendars/{calendarId}/{runId}", calendarId, UUID.randomUUID()),
        x -> x.post("/sync/calendars"));
  }

  @Test
  void run_get_shouldWork() {
    val accountId = dbHelper.createAccount(orgId);
    val calendarExternalId = TestData.calendarExternalId();
    val calendarId = dbHelper.createCalendar(orgId, accountId, calendarExternalId);
    val json = Map.of("calendarId", calendarId.value());

    // We can start a run, and the task will never be picked up because kafka producer is disabled.
    val runId = restAssuredJson(json)
        .post("/sync/calendars")
        .then()
        .statusCode(200)
        .body("id", not(blankOrNullString()))
        .extract()
        .body().jsonPath().getString("id");

    restAssured()
        .get("sync/calendars/{calendarId}/{runId}", calendarId.value(), runId)
        .then()
        .statusCode(200)
        .body("calendarId", is(calendarId.value()))
        .body("runId", is(runId))
        .body("status", is("pending"));
  }

  @Test
  void run_shouldReturn400ForInvalidCalendarId() {
    val invalid = CalendarId.create();
    val wrongOrg = dbHelper.createCalendar(TestData.orgId());
    val readOnly = Fluent
        .of(TestData.orgId())
        .map(x -> dbHelper.createCalendar(x, dbHelper.createAccount(x)))
        .get();
    val notExternal = dbHelper.createCalendar(orgId);

    Stream.of(invalid, wrongOrg, readOnly, notExternal).forEach(id ->
        restAssuredJson(Map.of("calendarId", id.value()))
            .post("/sync/calendars")
            .then()
            .statusCode(400)
            .body("error", not(blankOrNullString()))
            .body("violations.field", hasItems("calendarId")));
  }
}
