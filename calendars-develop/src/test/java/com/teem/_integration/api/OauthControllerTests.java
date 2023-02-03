package com.UoU._integration.api;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

import com.UoU._fakes.AuthServiceSpy;
import com.UoU._fakes.nylas.FakeNylasAuthService;
import com.UoU._fakes.oauth.FakeOauthClient;
import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU.core.accounts.Account;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.ServiceAccount;
import com.UoU.core.accounts.ServiceAccountAuthInfo;
import com.UoU.core.accounts.ServiceAccountCreateRequest;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.auth.AuthMethod;
import com.UoU.core.auth.OauthState;
import io.restassured.http.ContentType;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;

/**
 * Tests our oauth callback handling with the provider faked via FakeMicrosoftOauthHandler.
 */
public class OauthControllerTests extends BaseApiIntegrationTest {
  @Getter
  private final String basePath = "/oauth"; // use external unversioned url, not v1 url.

  @Autowired
  private AuthServiceSpy authServiceSpy;

  @AfterEach
  private void tearDown() {
    reset(authServiceSpy);
  }

  @Test
  void callback_shouldWorkForMicrosoftServiceAccountWithNewEmail() {
    val authCode = redisHelper.createAuthCode(orgId);
    val oauthState = new OauthState(AuthMethod.MS_OAUTH_SA, authCode);
    val oauthCode = TestData.uuidString();
    val email = FakeOauthClient.getFakeEmail(oauthCode);

    val result = doCallbackForServiceAccount(oauthCode, oauthState);
    val serviceAccount = result.getLeft();
    val authInfo = result.getRight();

    assertThat(serviceAccount.email())
        .as("Service account should have been created with expected email.")
        .isEqualTo(email);
    assertThat(authInfo).isNotNull();
    assertThat(redisHelper.getAuthCodeRepo().tryGet(authCode))
        .as("Auth code should have been deleted.")
        .isEmpty();
  }

  @Test
  void callback_shouldWorkForMicrosoftServiceAccountWithExistingEmail() {
    val authCode = redisHelper.createAuthCode(orgId);
    val oauthState = new OauthState(AuthMethod.MS_OAUTH_SA, authCode);
    val oauthCode = TestData.uuidString();
    val email = FakeOauthClient.getFakeEmail(oauthCode);

    // Create service account so it exists before auth.
    val id = TestData.serviceAccountId();
    dbHelper.getServiceAccountRepo().create(ServiceAccountCreateRequest.builder()
        .id(id)
        .orgId(orgId)
        .email(email)
        .settings(Map.of("test", "test"))
        .authMethod(AuthMethod.MS_OAUTH_SA)
        .build());

    // Do auth callback with existing email.
    val result = doCallbackForServiceAccount(oauthCode, oauthState);
    val serviceAccount = result.getLeft();
    val authInfo = result.getRight();

    assertThat(serviceAccount.id()).isEqualTo(id);
    assertThat(serviceAccount.email()).isEqualTo(email);
    assertThat(serviceAccount.updatedAt())
        .as("Service account should have been updated.")
        .isAfter(serviceAccount.createdAt());
    assertThat(authInfo).isNotNull();
    assertThat(redisHelper.getAuthCodeRepo().tryGet(authCode))
        .as("Auth code should have been deleted.")
        .isEmpty();
  }

  @Test
  void callback_shouldWorkForGoogleWithNewEmail() {
    val authCode = redisHelper.createAuthCode(orgId);
    val oauthState = new OauthState(AuthMethod.GOOGLE_OAUTH, authCode);
    val oauthCode = TestData.uuidString();
    val email = FakeOauthClient.getFakeEmail(oauthCode);

    val account = doCallbackForAccount(oauthCode, oauthState);

    assertThat(account.email())
        .as("Account should have been created with expected email.")
        .isEqualTo(email);
    assertThat(redisHelper.getAuthCodeRepo().tryGet(authCode))
        .as("Auth code should have been deleted.")
        .isEmpty();
  }

  @Test
  void callback_shouldWorkForGoogleWithExistingEmail() {
    val authCode = redisHelper.createAuthCode(orgId);
    val oauthState = new OauthState(AuthMethod.GOOGLE_OAUTH, authCode);
    val oauthCode = TestData.uuidString();
    val email = FakeOauthClient.getFakeEmail(oauthCode);

    // Create account so it exists before auth, and setup nylas fake to return same id.
    val id = dbHelper.createAccount(orgId, x -> x
        .email(email)
        .authMethod(AuthMethod.GOOGLE_OAUTH));
    FakeNylasAuthService.fakeAccountIdForEmail(email, id);

    val account = doCallbackForAccount(oauthCode, oauthState);

    assertThat(account.id()).isEqualTo(id);
    assertThat(account.email()).isEqualTo(email);
    assertThat(account.updatedAt())
        .as("Account should have been updated.")
        .isAfter(account.createdAt());
    assertThat(redisHelper.getAuthCodeRepo().tryGet(authCode))
        .as("Auth code should have been deleted.")
        .isEmpty();
  }

  @Test
  void callback_shouldWorkWithoutRedirectUrl() {
    val authCode = UUID.randomUUID();
    redisHelper.getAuthCodeRepo().create(ModelBuilders
        .authCodeCreateRequestWithTestData()
        .code(authCode)
        .redirectUri(null)
        .build());

    val oauthState = new OauthState(AuthMethod.MS_OAUTH_SA, authCode);
    val oauthCode = TestData.uuidString();

    restAssuredUnauthenticated()
        .queryParams("code", oauthCode, "state", oauthState.encode())
        .get("/callback")
        .then()
        .statusCode(200)
        .contentType(ContentType.HTML)
        .body(containsString("Success"));
  }

  @Test
  void callback_shouldWorkForV1Url() {
    val authCode = UUID.randomUUID();
    redisHelper.getAuthCodeRepo().create(ModelBuilders
        .authCodeCreateRequestWithTestData()
        .code(authCode)
        .redirectUri(null)
        .build());

    val oauthState = new OauthState(AuthMethod.MS_OAUTH_SA, authCode);
    val oauthCode = TestData.uuidString();

    restAssuredUnauthenticated()
        .basePath("/v1/oauth")
        .queryParams("code", oauthCode, "state", oauthState.encode())
        .get("/callback")
        .then()
        .statusCode(200)
        .contentType(ContentType.HTML)
        .body(containsString("Success"));
  }

  @Test
  void callback_shouldReturn404ForEmptyParams() {
    // With no params, the route should not even resolve, so should be 404.
    restAssuredUnauthenticated()
        .queryParams("code", "", "state", "")
        .get("/callback")
        .then()
        .statusCode(404);
  }

  @Test
  void callback_shouldReturn404ForWrongErrorFormat() {
    // Errors are_like_this, so the route should not even resolve otherwise, so should be 404.
    restAssuredUnauthenticated()
        .queryParam("error", "invalid-format-for-oauth")
        .get("/callback")
        .then()
        .statusCode(404);
  }

  @Test
  void callback_shouldReturnFriendly400ForInvalidParams() {
    restAssuredUnauthenticated()
        .queryParams("code", "invalid", "state", "invalid")
        .get("/callback")
        .then()
        .statusCode(400)
        .contentType(ContentType.HTML)
        .body(containsString("Invalid auth request"));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      OAuth2ErrorCodes.ACCESS_DENIED,
  })
  void callback_shouldReturn400ForOauthErrorCodes(String error) {
    restAssuredUnauthenticated()
        .queryParam("error", error)
        .get("/callback")
        .then()
        .statusCode(400)
        .contentType(ContentType.HTML);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      OAuth2ErrorCodes.SERVER_ERROR,
      OAuth2ErrorCodes.TEMPORARILY_UNAVAILABLE,
      "any_other_code_that_matches_the_oauth_format"
  })
  void callback_shouldReturn500ForOauthErrorCodes(String error) {
    restAssuredUnauthenticated()
        .queryParam("error", error)
        .get("/callback")
        .then()
        .statusCode(500)
        .contentType(ContentType.HTML);
  }

  @Test
  void callback_should500ForOauthClientTokenExchangeException() {
    val authCode = redisHelper.createAuthCode(orgId);
    val oauthState = new OauthState(AuthMethod.GOOGLE_OAUTH, authCode);
    val oauthCode = FakeOauthClient.getFailureCode(); // causes exception

    restAssuredUnauthenticated()
        .queryParams("code", oauthCode, "state", oauthState.encode())
        .get("/callback")
        .then()
        .statusCode(500)
        .contentType(ContentType.HTML)
        .body(containsString("Error"));
  }

  @Test
  void callback_should500ForAuthServiceException() {
    val authCode = redisHelper.createAuthCode(orgId);
    val oauthState = new OauthState(AuthMethod.MS_OAUTH_SA, authCode);
    val oauthCode = TestData.uuidString();

    doThrow(new RuntimeException("ERROR!!!!!!!"))
        .when(authServiceSpy).handleOauthCallback(any(), any());

    restAssuredUnauthenticated()
        .queryParams("code", oauthCode, "state", oauthState.encode())
        .get("/callback")
        .then()
        .statusCode(500)
        .contentType(ContentType.HTML)
        .body(containsString("Error"));
  }

  @Test
  void success_shouldWork() {
    restAssuredUnauthenticated()
        .get("/success")
        .then()
        .statusCode(200)
        .contentType(ContentType.HTML)
        .body(containsString("Success"));
  }

  @Test
  void success_shouldWorkForV1Url() {
    restAssuredUnauthenticated()
        .basePath("/v1/oauth")
        .get("/success")
        .then()
        .statusCode(200)
        .contentType(ContentType.HTML)
        .body(containsString("Success"));
  }

  private Pair<ServiceAccount, ServiceAccountAuthInfo> doCallbackForServiceAccount(
      String oauthCode, OauthState oauthState) {

    val redirectUrl = restAssuredUnauthenticated()
        .queryParams("code", oauthCode, "state", oauthState.encode())
        .redirects().follow(false)
        .get("/callback")
        .then()
        .statusCode(302)
        .extract()
        .header("Location");

    val idString = redirectUrl.split("calendarsApiServiceAccountId=")[1];
    val id = new ServiceAccountId(UUID.fromString(idString));
    return Pair.of(
        dbHelper.getServiceAccountRepo().get(id),
        dbHelper.getServiceAccountRepo().getAuthInfo(id));
  }

  private Account doCallbackForAccount(String oauthCode, OauthState oauthState) {
    val redirectUrl = restAssuredUnauthenticated()
        .queryParams("code", oauthCode, "state", oauthState.encode())
        .redirects().follow(false)
        .get("/callback")
        .then()
        .statusCode(302)
        .extract()
        .header("Location");

    val idString = redirectUrl.split("calendarsApiAccountId=")[1];
    val id = new AccountId(idString);
    return dbHelper.getAccountRepo().get(id);
  }
}
