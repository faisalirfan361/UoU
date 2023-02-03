package com.UoU._integration.api.v1;

import static org.hamcrest.Matchers.containsString;

import com.UoU._helpers.TestData;
import com.UoU._integration.api.BaseApiIntegrationTest;
import com.UoU.app.security.Scopes;
import com.UoU.core.calendars.CalendarId;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Test;

class AdminControllerTests extends BaseApiIntegrationTest {
  @Getter private final String basePath = "/v1/admin";

  @Test
  void all_shouldBeAuthorizedByValidScopes() {
    auth.assertEachScopeAuthorizes(
        List.of(Scopes.ADMIN),
        x -> x.put("/sync/accounts/{id}", TestData.accountId().value()),
        x -> x.put("/sync/accounts/{id}/sync-state", TestData.accountId().value()),
        x -> x.put("/sync/accounts/{id}/restart", TestData.accountId().value()),
        x -> x.put("/sync/calendars/{id}", CalendarId.create().value()));
  }

  @Test
  void syncAccount_shouldWork() {
    val id = dbHelper.createAccount(orgId).value();

    restAssured()
        .put("/sync/accounts/{id}", id)
        .then()
        .statusCode(204);
  }

  @Test
  void syncCalendar_shouldWork() {
    val externalId = TestData.calendarExternalId();
    val accountId = dbHelper.createAccount(orgId);
    val id = dbHelper.createCalendar(orgId, accountId, externalId).value();

    restAssured()
        .put("/sync/calendars/{id}", id)
        .then()
        .statusCode(204);
  }

  @Test
  void syncCalendar_should400WhenNoExternalId() {
    val id = dbHelper.createCalendar(orgId, dbHelper.createAccount(orgId)).value();

    restAssured()
        .put("/sync/calendars/{id}", id)
        .then()
        .statusCode(400)
        .body("error", containsString("external"));
  }
}
