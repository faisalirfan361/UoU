package com.UoU._integration.api.v1;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.oneOf;

import com.UoU._helpers.TestData;
import com.UoU._integration.api.BaseApiIntegrationTest;
import com.UoU.app.security.Scopes;
import com.UoU.core.accounts.AccountError;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.Getter;
import org.junit.jupiter.api.Test;

class AccountControllerTests extends BaseApiIntegrationTest {
  private static final String[] AUTH_METHODS = new String[]{"ms-oauth-sa", "google-oauth"};

  @Getter private final String basePath = "/v1/accounts";

  @Test
  void reads_shouldBeAuthorizedByValidScopes() {
    auth.assertEachScopeAuthorizes(
        List.of(Scopes.ACCOUNTS, Scopes.ACCOUNTS_READONLY),
        x -> x.get(), // list
        x -> x.get(TestData.uuidString()), // get by id
        x -> x.get("/{id}/errors", TestData.uuidString())); // list errors
  }

  @Test
  void writes_shouldBeAuthorizedByValidScopes() {
    auth.assertScopeAuthorizes(
        Scopes.ACCOUNTS,
        x -> x.delete(TestData.uuidString())); // delete by id
  }

  @Test
  void list_shouldWork() {
    var ids = Stream
        .generate(() -> dbHelper.createAccount(orgId))
        .limit(2)
        .map(x -> x.value())
        .toArray(String[]::new);

    // Create one account in different org to make sure it isn't returned.
    dbHelper.createAccount(TestData.orgId());

    // Page 1:
    var nextCursor = restAssured()
        .queryParam("limit", 1)
        .get()
        .then()
        .statusCode(200)
        .body("items.size()", is(1))
        .body("items.id", everyItem(oneOf(ids)))
        .body("items.authMethod", everyItem(oneOf(AUTH_METHODS)))
        .body("meta.nextCursor", not(blankOrNullString()))
        .extract().jsonPath().getString("meta.nextCursor");

    // Page 2:
    restAssured()
        .queryParam("cursor", nextCursor)
        .get()
        .then()
        .statusCode(200)
        .body("items.size()", is(1))
        .body("items.id", everyItem(oneOf(ids)))
        .body("meta.nextCursor", nullValue());
  }

  @Test
  void list_should400ForInvalidCursor() {
    restAssured()
        .queryParam("cursor", "invalid")
        .get()
        .then()
        .statusCode(400)
        .body("error", containsString("cursor"));
  }

  @Test
  void getById_shouldWork() {
    var id = dbHelper.createAccount(orgId).value();

    restAssured()
        .get(id)
        .then()
        .statusCode(200)
        .body("id", is(id))
        .body("email", not(blankOrNullString()));
  }

  @Test
  void getByIdWithServiceAccount_shouldWork() {
    var serviceAccId = dbHelper.createServiceAccount(orgId);
    var id = dbHelper.createSubaccount(orgId, serviceAccId).value();

    restAssured()
        .get(id)
        .then()
        .statusCode(200)
        .body("id", is(id))
        .body("serviceAccountId", is(serviceAccId.value().toString()));
  }

  @Test
  void getById_shouldReturn404() {
    restAssured()
        .get(UUID.randomUUID().toString())
        .then()
        .statusCode(404);
  }

  @Test
  void listErrorsByAccountId_shouldWork() {
    var id = dbHelper.createAccount(orgId);
    var error = new AccountError(id, AccountError.Type.AUTH, TestData.uuidString(), "details");
    dbHelper.getAccountRepo().createError(error);

    restAssured()
        .get("/{id}/errors", id.value())
        .then()
        .statusCode(200)
        .body("id", contains(error.id().toString()))
        .body("message", contains(error.message()));
  }

  @Test
  void delete_shouldReturnNoContent() {
    var accountId = dbHelper.createAccount(orgId);
    var calendarId = dbHelper.createCalendar(orgId, accountId);
    dbHelper.createEvent(orgId, calendarId, 2); // create with participants

    // Unrelated data to make sure it's not deleted:
    var otherCalendarId = dbHelper.createCalendar(orgId, dbHelper.createAccount(orgId));
    dbHelper.createEvent(orgId, otherCalendarId, 2);

    restAssured()
        .delete(accountId.value())
        .then()
        .statusCode(204);

    // Check that delete worked:
    restAssured()
        .delete(accountId.value())
        .then()
        .statusCode(404);

    // Check that unrelated things weren't deleted.
  }

  @Test
  void delete_shouldReturn404() {
    restAssured()
        .delete(UUID.randomUUID().toString())
        .then()
        .statusCode(404);
  }
}
