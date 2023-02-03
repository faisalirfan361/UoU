package com.UoU._integration.api.v1;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.oneOf;

import com.UoU._fakes.nylas.FakeNylasAuthService;
import com.UoU._helpers.TestData;
import com.UoU._integration.api.BaseApiIntegrationTest;
import com.UoU.app.security.Scopes;
import com.UoU.core.accounts.AccountId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Test;

class ServiceAccountControllerTests extends BaseApiIntegrationTest {
  private static final String[] AUTH_METHODS = new String[]{"ms-oauth-sa"};

  @Getter
  private final String basePath = "/v1/serviceaccounts";

  @Test
  void reads_shouldBeAuthorizedByValidScopes() {
    auth.assertEachScopeAuthorizes(
        List.of(Scopes.ACCOUNTS, Scopes.ACCOUNTS_READONLY),
        x -> x.get(), // list
        x -> x.get(TestData.uuidString())); // get by id
  }

  @Test
  void writes_shouldBeAuthorizedByValidScopes() {
    auth.assertScopeAuthorizes(
        Scopes.ACCOUNTS,
        x -> x.delete(TestData.uuidString()),
        x -> x.post("/{id}/accounts", TestData.uuidString())); // create subaccount
  }

  @Test
  void list_shouldWork() {
    val ids = Stream
        .generate(() -> dbHelper.createServiceAccount(orgId))
        .limit(2)
        .map(x -> x.value().toString())
        .toArray(String[]::new);

    // Create one in a different org to make sure it's filtered out.
    dbHelper.createServiceAccount(TestData.orgId());

    // Page 1:
    val nextCursor = restAssured()
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
        .body("items.authMethod", everyItem(oneOf(AUTH_METHODS)))
        .body("meta.nextCursor", nullValue());
  }

  @Test
  void getById_shouldWork() {
    val id = dbHelper.createServiceAccount(orgId).value().toString();

    restAssured()
        .get(id)
        .then()
        .statusCode(200)
        .body("id", is(id))
        .body("email", not(blankOrNullString()))
        .body("authMethod", oneOf(AUTH_METHODS));
  }

  @Test
  void getById_shouldReturn404() {
    restAssured()
        .get(TestData.uuidString())
        .then()
        .statusCode(404);
  }

  @Test
  void delete_shouldReturnNoContent() {
    val id = dbHelper.createServiceAccount(orgId).value().toString();

    restAssured()
        .delete(id)
        .then()
        .statusCode(204);

    // Check that delete worked:
    restAssured()
        .delete(id)
        .then()
        .statusCode(404);
  }

  @Test
  void delete_shouldReturn404() {
    restAssured()
        .delete(UUID.randomUUID().toString())
        .then()
        .statusCode(404);
  }

  @Test
  void createAccount_shouldWorkForNewAndExistingEmail() {
    val serviceAccountId = dbHelper.createServiceAccount(orgId);
    val accountEmail = TestData.email();

    val json = Map.of(
        "email", accountEmail,
        "name", TestData.uuidString()
    );

    // New email - account should be created:
    val accountId = restAssuredJson(json)
        .post("/{id}/accounts", serviceAccountId.value())
        .then()
        .statusCode(200)
        .extract()
        .path("id")
        .toString();

    // Existing email - account should be updated but return same account id:
    FakeNylasAuthService.fakeAccountIdForEmail(accountEmail, new AccountId(accountId));
    val accountId2 = restAssuredJson(json)
        .post("/{id}/accounts", serviceAccountId.value())
        .then()
        .statusCode(200)
        .extract()
        .path("id")
        .toString();

    assertThat(accountId).isNotNull();
    assertThat(accountId).isEqualTo(accountId2);

    // Check that create worked properly:
    restAssured("/v1/accounts")
        .get(accountId)
        .then()
        .statusCode(200)
        .body("id", is(accountId))
        .body("serviceAccountId", is(serviceAccountId.value().toString()))
        .body("email", is(json.get("email")));
  }

  @Test
  void createAccount_shouldReturn400WhenInvalid() {
    val json = "{\"invalid\": true}";

    restAssuredJson(json)
        .post("/{id}/accounts", TestData.uuidString())
        .then()
        .statusCode(400)
        .body("error", not(blankOrNullString()))
        .body("violations.field", containsInAnyOrder("email", "name"));
  }

  @Test
  void listByAccount_shouldPage() {
    val serviceAccountId = dbHelper.createServiceAccount(orgId);
    val accountIds = Stream
        .generate(() -> dbHelper.createSubaccount(orgId, serviceAccountId).value())
        .limit(2)
        .toArray(String[]::new);

    // Page 1:
    val nextCursor = restAssured()
        .queryParam("limit", 1)
        .get("{id}/accounts", serviceAccountId.value())
        .then()
        .statusCode(200)
        .body("items.size()", is(1))
        .body("items.id", everyItem(oneOf(accountIds)))
        .body("meta.nextCursor", not(blankOrNullString()))
        .extract().jsonPath().getString("meta.nextCursor");

    // Page 2:
    restAssured()
        .queryParam("cursor", nextCursor)
        .get("{id}/accounts", serviceAccountId.value())
        .then()
        .statusCode(200)
        .body("items.size()", is(1))
        .body("items.id", everyItem(oneOf(accountIds)))
        .body("meta.nextCursor", nullValue());
  }
}
