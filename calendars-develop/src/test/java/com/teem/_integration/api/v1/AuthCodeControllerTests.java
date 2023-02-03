package com.UoU._integration.api.v1;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.not;

import com.UoU._integration.api.BaseApiIntegrationTest;
import com.UoU.app.security.Scopes;
import com.UoU.core.auth.AuthConstraints;
import java.util.Map;
import lombok.Getter;
import org.junit.jupiter.api.Test;

class AuthCodeControllerTests extends BaseApiIntegrationTest {
  @Getter private final String basePath = "/v1/auth";

  @Test
  void writes_shouldBeAuthorizedByValidScopes() {
    auth.assertScopeAuthorizes(
        Scopes.ACCOUNTS,
        x -> x.post("/codes")); // create
  }

  @Test
  void generate_shouldWorkWithoutBody() {
    restAssuredJson()
        .post("/codes")
        .then()
        .statusCode(201)
        .body("code", hasLength(36));
  }

  @Test
  void generate_shouldWorkWithRedirectUri() {
    var json = Map.of("redirectUri", "https://example.com");
    restAssuredJson(json)
        .post("/codes")
        .then()
        .statusCode(201)
        .body("code", hasLength(36));
  }

  @Test
  void generate_should400ForInvalidRedirectUri() {
    var json = Map.of("redirectUri", "not-a-url");
    restAssuredJson(json)
        .post("/codes")
        .then()
        .statusCode(400)
        .body("error", not(blankOrNullString()));
  }

  @Test
  void generate_should400ForTooLongRedirectUri() {
    var json = Map.of(
        "redirectUri",
        "https://" + "x".repeat(AuthConstraints.REDIRECT_URI_MAX - 11) + ".com");
    restAssuredJson(json)
        .post("/codes")
        .then()
        .statusCode(400)
        .body("error", not(blankOrNullString()));
  }
}
