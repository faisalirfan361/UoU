package com.UoU._integration.api.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.oneOf;

import com.UoU._helpers.TestData;
import com.UoU._integration.api.BaseApiIntegrationTest;
import com.UoU.app.security.Scopes;
import com.UoU.core.auth.AuthMethod;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Test;

class CalendarControllerTests extends BaseApiIntegrationTest {
  @Getter private final String basePath = "/v1/calendars";

  @Test
  void reads_shouldBeAuthorizedByValidScopes() {
    auth.assertEachScopeAuthorizes(
        List.of(Scopes.CALENDARS, Scopes.CALENDARS_READONLY),
        x -> x.get("/byaccount/{accountId}", TestData.uuidString()), // list by account
        x -> x.get(TestData.uuidString())); // get by id
  }

  @Test
  void writes_shouldBeAuthorizedByValidScopes() {
    auth.assertEachScopeAuthorizes(
        List.of(Scopes.CALENDARS),
        x -> x.post(), // create internal calendar
        x -> x.post("/batch"), // batch create internal calendars
        x -> x.put(TestData.uuidString()), // update by id
        x -> x.delete(TestData.uuidString())); // delete by id
  }

  @Test
  void listByAccount_shouldIncludeWritableCalendarsInOrg() {
    val accountId = dbHelper.createAccount(orgId);
    val calendarIds = Stream
        .generate(() -> dbHelper.createCalendar(orgId, accountId).value())
        .limit(2)
        .toArray(String[]::new);

    // Create a read-only calendar to make sure it's filtered out.
    dbHelper.createCalendar(orgId, x -> x.accountId(accountId).isReadOnly(true));

    // Create a calendar in a different org to make sure it's filtered out.
    // This shouldn't happen in real life, but we want to ensure the calendar.org_id is checked.
    dbHelper.createCalendar(TestData.orgId(), accountId);

    restAssured()
        .get("/byaccount/{acccountId}", accountId.value())
        .then()
        .statusCode(200)
        .body("items.id", containsInAnyOrder(calendarIds))
        .body("items.accountId", everyItem(is(accountId.value())))
        .body("meta", hasKey("nextCursor"));
  }

  @Test
  void listByAccount_shouldPage() {
    val accountId = dbHelper.createAccount(orgId);
    val calendarIds = Stream
        .generate(() -> dbHelper.createCalendar(orgId, accountId).value())
        .limit(2)
        .toArray(String[]::new);

    // Page 1:
    val nextCursor = restAssured()
        .queryParam("limit", 1)
        .get("/byaccount/{acccountId}", accountId.value())
        .then()
        .statusCode(200)
        .body("items.size()", is(1))
        .body("items.id", everyItem(oneOf(calendarIds)))
        .body("meta.nextCursor", not(blankOrNullString()))
        .extract().jsonPath().getString("meta.nextCursor");

    // Page 2:
    restAssured()
        .queryParam("cursor", nextCursor)
        .get("/byaccount/{acccountId}", accountId.value())
        .then()
        .statusCode(200)
        .body("items.size()", is(1))
        .body("items.id", everyItem(oneOf(calendarIds)))
        .body("meta.nextCursor", nullValue());
  }

  @Test
  void getById_shouldWork() {
    val id = dbHelper.createCalendar(orgId).value();

    restAssured()
        .get(id)
        .then()
        .statusCode(200)
        .body("id", is(id))
        .body("name", not(blankOrNullString()));
  }

  @Test
  void getById_shouldWorkForReadOnlyCalendar() {
    val id = dbHelper.createCalendar(orgId, x -> x.isReadOnly(true)).value();

    restAssured()
        .get(id)
        .then()
        .statusCode(200)
        .body("id", is(id))
        .body("name", not(blankOrNullString()));
  }

  @Test
  void getById_shouldReturn404() {
    restAssured()
        .get(TestData.uuidString())
        .then()
        .statusCode(404);
  }

  @Test
  void createInternal_shouldWork() {
    val json = Map.of("name", "test", "timezone", "UTC");
    restAssuredJson(json)
        .post()
        .then()
        .statusCode(201)
        .body("id", not(blankOrNullString()))
        .body("email", containsString("@"))
        .body("name", equalTo("test"));
  }

  @Test
  void batchCreateInternal_shouldWork() {
    val json = Map.of(
        "start", 1,
        "end", 3,
        "increment", 1,
        "namePattern", "Test {n}",
        "timezone", "America/New_York");

    restAssuredJson(json)
        .post("/batch")
        .then()
        .statusCode(201)
        .body("1.id", not(blankOrNullString()))
        .body("2.id", not(blankOrNullString()))
        .body("3.id", not(blankOrNullString()));
  }

  /**
   * Internal calendar should allow change of name and timezone.
   */
  @Test
  void update_internal_shouldChangeNameAndTimezone() {
    val id = dbHelper.createCalendarWithAccount(
        orgId,
        account -> account.authMethod(AuthMethod.INTERNAL),
        cal -> cal.timezone("UTC"));

    val json = updateJson("changed name", "Africa/Cairo");
    restAssuredJson(json)
        .put(id.value())
        .then()
        .statusCode(204);

    val result = dbHelper.getCalendar(id);
    assertThat(result.getName()).isEqualTo(json.get("name"));
    assertThat(result.getTimezone()).isEqualTo(json.get("timezone"));
  }

  /**
   * Calendar without account should be treated just like an internal calendar and allow changes.
   */
  @Test
  void update_noAccount_shouldChangeNameAndTimezone() {
    val id = dbHelper.createCalendar(orgId, x -> x.timezone("UTC"));

    val json = updateJson("changed name", "Africa/Cairo");
    restAssuredJson(json)
        .put(id.value())
        .then()
        .statusCode(204);

    val result = dbHelper.getCalendar(id);
    assertThat(result.getName()).isEqualTo(json.get("name"));
    assertThat(result.getTimezone()).isEqualTo(json.get("timezone"));
  }

  /**
   * MS calendar should allow only timezone to change.
   */
  @Test
  void update_microsoft_shouldChangeTimezone() {
    val name = TestData.uuidString();
    val id = dbHelper.createCalendarWithAccount(
        orgId,
        account -> account.authMethod(AuthMethod.MS_OAUTH_SA),
        cal -> cal.name(name).timezone("UTC"));

    val json = updateJson(name, "Africa/Cairo");
    restAssuredJson(json)
        .put(id.value())
        .then()
        .statusCode(204);

    val result = dbHelper.getCalendar(id);
    assertThat(result.getName()).isEqualTo(name);
    assertThat(result.getTimezone()).isEqualTo(json.get("timezone"));
  }

  @Test
  void update_microsoft_shouldFailOnNameChange() {
    val timezone = "UTC";
    val id = dbHelper.createCalendarWithAccount(
        orgId,
        account -> account.authMethod(AuthMethod.MS_OAUTH_SA),
        cal -> cal.timezone(timezone));

    val json = updateJson("changed name", timezone);
    restAssuredJson(json)
        .put(id.value())
        .then()
        .statusCode(400)
        .body("violations.field", contains("name"));
  }

  /**
   * Google calendar should allow existing values but no changes.
   */
  @Test
  void update_google_shouldAllowExistingValuesWithNoChanges() {
    val name = TestData.uuidString();
    val timezone = "America/Rainy_River";
    val id = dbHelper.createCalendarWithAccount(
        orgId,
        account -> account.authMethod(AuthMethod.GOOGLE_OAUTH),
        cal -> cal.name(name).timezone(timezone));

    val json = updateJson(name, timezone);
    restAssuredJson(json)
        .put(id.value())
        .then()
        .statusCode(204);

    val result = dbHelper.getCalendar(id);
    assertThat(result.getName()).isEqualTo(name);
    assertThat(result.getTimezone()).isEqualTo(timezone);
  }

  @Test
  void update_google_shouldFailOnNameChange() {
    val timezone = "UTC";
    val id = dbHelper.createCalendarWithAccount(
        orgId,
        account -> account.authMethod(AuthMethod.GOOGLE_SA),
        cal -> cal.timezone(timezone));

    val json = updateJson("changed name", timezone);
    restAssuredJson(json)
        .put(id.value())
        .then()
        .statusCode(400)
        .body("violations.field", contains("name"));
  }

  @Test
  void update_google_shouldFailOnTimezoneChange() {
    val name = "calendar name";
    val id = dbHelper.createCalendarWithAccount(
        orgId,
        account -> account.authMethod(AuthMethod.GOOGLE_OAUTH),
        cal -> cal.name(name));

    val json = updateJson(name, "UTC");
    restAssuredJson(json)
        .put(id.value())
        .then()
        .statusCode(400)
        .body("violations.field", contains("timezone"));
  }

  @Test
  void update_shouldFailForReadOnlyCalendar() {
    val id = dbHelper.createCalendar(orgId, x -> x.isReadOnly(true)).value();
    val json = updateJson("name", "UTC");

    restAssuredJson(json)
        .put(id)
        .then()
        .statusCode(400)
        .body("error", containsString("read-only"));
  }

  @Test
  void update_shouldReturn404() {
    val id = TestData.uuidString();
    val json = updateJson("name", "UTC");

    restAssuredJson(json)
        .put(id)
        .then()
        .statusCode(404);
  }

  @Test
  void delete_internal_shouldWork() {
    val id = dbHelper.createCalendarWithAccount(
        orgId,
        account -> account.authMethod(AuthMethod.INTERNAL));

    restAssured()
        .delete(id.value())
        .then()
        .statusCode(204);
  }

  @Test
  void delete_noAccount_shouldWork() {
    val id = dbHelper.createCalendar(orgId);

    restAssured()
        .delete(id.value())
        .then()
        .statusCode(204);
  }

  @Test
  void delete_microsoft_shouldFail() {
    val id = dbHelper.createCalendarWithAccount(
        orgId,
        account -> account.authMethod(AuthMethod.MS_OAUTH_SA));

    restAssured()
        .delete(id.value())
        .then()
        .statusCode(400)
        .body("error", containsString("Microsoft"));
  }

  @Test
  void delete_google_shouldFail() {
    val id = dbHelper.createCalendarWithAccount(
        orgId,
        account -> account.authMethod(AuthMethod.GOOGLE_OAUTH));

    restAssured()
        .delete(id.value())
        .then()
        .statusCode(400)
        .body("error", containsString("Google"));
  }

  @Test
  void delete_shouldReturn404() {
    restAssured()
        .delete(TestData.uuidString())
        .then()
        .statusCode(404);
  }

  private Map<String, Object> updateJson(String name, String timezone) {
    return Map.of("name", name, "timezone", timezone);
  }
}
