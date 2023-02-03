package com.UoU._integration.api.v1.unauthenticated;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

import com.UoU._fakes.AuthServiceSpy;
import com.UoU._helpers.TestData;
import com.UoU._integration.api.BaseApiIntegrationTest;
import com.UoU.core.auth.AuthMethod;
import io.restassured.http.ContentType;
import java.time.Duration;
import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

// TODO: This class should be combined with ../AuthConnectControllerTests.java.
public class AuthConnectControllerTests extends BaseApiIntegrationTest {
  @Getter
  private final String basePath = "/v1/auth/connect";

  @Autowired
  private AuthServiceSpy authServiceSpy;

  @AfterEach
  private void tearDown() {
    reset(authServiceSpy);
  }

  @Test
  void all_should404ForInvalidCode() {
    var code = "invalid-uuid";

    // ui:
    restAssuredUnauthenticated()
        .get("/{code}", code)
        .then()
        .statusCode(404)
        .contentType(ContentType.HTML)
        .body(containsString("Invalid auth code"));

    // specific method:
    restAssuredUnauthenticated()
        .get("/{method}/{code}", AuthMethod.MS_OAUTH_SA.getValue(), code)
        .then()
        .statusCode(404)
        .contentType(ContentType.HTML)
        .body(containsString("Invalid auth code"));
  }

  @Test
  void all_should404ForExpiredCode() {
    var code = redisHelper.createAuthCode(TestData.orgId(), Duration.ZERO);

    // ui:
    restAssuredUnauthenticated()
        .get("/{code}", code)
        .then()
        .statusCode(404)
        .contentType(ContentType.HTML)
        .body(containsString("Invalid auth code"));

    // specific method:
    restAssuredUnauthenticated()
        .get("/{method}/{code}", AuthMethod.MS_OAUTH_SA.getValue(), code)
        .then()
        .statusCode(404)
        .contentType(ContentType.HTML)
        .body(containsString("Invalid auth code"));
  }

  @Test
  void ui_shouldWork() {
    var code = redisHelper.createAuthCode(TestData.orgId());

    restAssuredUnauthenticated()
        .get("/{code}", code)
        .then()
        .statusCode(200)
        .contentType(ContentType.HTML)
        .body(containsString("Authorize your account"));
  }

  @Test
  void specificMethod_shouldRedirectToMicrosoftOauth() {
    var code = redisHelper.createAuthCode(TestData.orgId());

    restAssuredUnauthenticated()
        .redirects().follow(false)
        .get("/{method}/{code}", AuthMethod.MS_OAUTH_SA.getValue(), code)
        .then()
        .statusCode(302)
        .header("Location", allOf(
            startsWith("https://login.microsoftonline.com/"),
            containsString("response_type=code"),
            containsString("redirect_uri="),
            containsString("scope="),
            containsString("state=")));
  }

  @Test
  void specificMethod_shouldRedirectToGoogleOauth() {
    var code = redisHelper.createAuthCode(TestData.orgId());

    restAssuredUnauthenticated()
        .redirects().follow(false)
        .get("/{method}/{code}", AuthMethod.GOOGLE_OAUTH.getValue(), code)
        .then()
        .statusCode(302)
        .header("Location", allOf(
            startsWith("https://accounts.google.com/"),
            containsString("response_type=code"),
            containsString("redirect_uri="),
            containsString("scope="),
            containsString("state=")));
  }

  @Test
  void specificMethod_should404ForInvalidMethod() {
    var code = redisHelper.createAuthCode(TestData.orgId());

    // specific method:
    restAssuredUnauthenticated()
        .get("/{method}/{code}", "invalid-method", code)
        .then()
        .statusCode(404)
        .contentType(ContentType.HTML)
        .body(containsString("Invalid auth method"));
  }

  @Test
  void specificMethod_should404ForInternalMethod() {
    var code = redisHelper.createAuthCode(TestData.orgId());

    // specific method:
    restAssuredUnauthenticated()
        .get("/{method}/{code}", "internal", code)
        .then()
        .statusCode(404)
        .contentType(ContentType.HTML)
        .body(containsString("Invalid auth method"));
  }

  @Test
  void specificMethod_should500ForAuthServiceException() {
    var code = redisHelper.createAuthCode(TestData.orgId());

    doThrow(new RuntimeException("No url for you"))
        .when(authServiceSpy).getOauthRedirectUrl(any(), any());

    restAssuredUnauthenticated()
        .redirects().follow(false)
        .get("/{method}/{code}", AuthMethod.MS_OAUTH_SA.getValue(), code)
        .then()
        .statusCode(500)
        .contentType(ContentType.HTML)
        .body(containsString("Error"));
  }
}
