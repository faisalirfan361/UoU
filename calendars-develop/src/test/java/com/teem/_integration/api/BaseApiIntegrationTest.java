package com.UoU._integration.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.UoU._helpers.TestData;
import com.UoU._integration.BaseAppIntegrationTest;
import com.UoU.app.security.CustomClaims;
import com.UoU.app.security.Scopes;
import com.UoU.core.Fluent;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Base class for integration tests of full API requests.
 *
 * <p>This is setup with REST-assured to make and validate requests, along with some other helpers.
 */
public abstract class BaseApiIntegrationTest extends BaseAppIntegrationTest {

  protected abstract String getBasePath();

  @LocalServerPort
  private int port;

  @Value("${jwt.audience}")
  private String jwtAudience;

  @Value("${integration-tests.private-jwk}")
  private String privateJwk;

  /**
   * Helper for authorizing requests.
   */
  protected Auth auth;

  @PostConstruct
  private void init() {
    this.auth = new Auth();
  }

  /**
   * Gets a REST-assured spec with the default base path returned by {@link #getBasePath()}.
   */
  protected RequestSpecification restAssured() {
    return restAssured(getBasePath());
  }

  /**
   * Gets a REST-assured spec with a custom base path.
   *
   * <p>Usually, you'll want to call {@link #restAssured()} and use the default path instead.
   *
   * <p>The request will be authorized with a JWT that allows full access by default. To override,
   * replace the JWT with something like `.auth().oauth2(anotherJwtString)` or `.auth().none()`.
   * The `auth` property contains some helpers to help you build JWTs with the claims you want.
   */
  protected RequestSpecification restAssured(String basePath) {
    return given()
        .baseUri("http://localhost")
        .port(port)
        .basePath(basePath)
        .auth().oauth2(auth.createJwtWithFullAccess())
        .config(RestAssuredConfig.config()
            .logConfig(LogConfig.logConfig()
                .enableLoggingOfRequestAndResponseIfValidationFails()
                .enablePrettyPrinting(true)));
  }

  /**
   * Gets a REST-assured spec with the auth removed for testing unauthenticated endpoints.
   */
  protected RequestSpecification restAssuredUnauthenticated() {
    return restAssured().auth().none();
  }

  /**
   * Gets a REST-assured spec pre-configured for an application/json content-type.
   */
  protected <T> RequestSpecification restAssuredJson() {
    return restAssured(getBasePath()).contentType(MediaType.APPLICATION_JSON.toString());
  }

  /**
   * Shortcut that calls {@link #restAssuredJson()} and then adds the json body.
   */
  protected <T> RequestSpecification restAssuredJson(Map<String, T> body) {
    return restAssuredJson().body(body);
  }

  /**
   * Shortcut that calls {@link #restAssuredJson()} and then adds the json body.
   */
  protected <T> RequestSpecification restAssuredJson(String body) {
    return restAssuredJson().body(body);
  }

  /**
   * Helper for auth-related setup and testing.
   */
  protected class Auth {
    private final NimbusJwtEncoder jwtEncoder;

    @SneakyThrows
    public Auth() {
      jwtEncoder = new NimbusJwtEncoder(
          new ImmutableJWKSet<>(
              new JWKSet(
                  JWK.parse(privateJwk))));
    }

    /**
     * Builds a valid set of claims for full API access, which you can then further customize.
     */
    public JwtClaimsSet.Builder buildClaimsWithFullAccess() {
      return JwtClaimsSet.builder()
          .subject(TestData.email())
          .claim(CustomClaims.ORG_ID, orgId.value())
          .audience(List.of(jwtAudience))
          .issuedAt(Instant.now().minusSeconds(300))
          .expiresAt(Instant.now().plusSeconds(300))
          .claim(CustomClaims.SCOPE, getAllAccessScope());
    }

    public String getAllAccessScope() {
      return String.join(" ", List.of(
          Scopes.ACCOUNTS,
          Scopes.CALENDARS,
          Scopes.EVENTS,
          Scopes.DIAGNOSTICS,
          Scopes.ADMIN
      ));
    }

    public String createJwt(JwtClaimsSet claims) {
      return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String createJwtWithFullAccess() {
      return createJwt(buildClaimsWithFullAccess().build());
    }

    public String createJwtWithScope(String... scope) {
      return createJwt(
          buildClaimsWithFullAccess()
              .claim(CustomClaims.SCOPE, String.join(" ", scope))
              .build());
    }

    /**
     * Checks that the passed scope grants access for each passed request.
     */
    @SafeVarargs
    public final void assertScopeAuthorizes(
        String scope,
        Function<RequestSpecification, Response>... requests) {
      assertEachScopeAuthorizes(List.of(scope), requests);
    }

    /**
     * Checks that each passed scope grants access for each passed request.
     */
    @SafeVarargs
    public final void assertEachScopeAuthorizes(
        List<String> scopes,
        Function<RequestSpecification, Response>... requests) {

      val jsonBody = "{}"; // doesn't need to be valid for requests

      for (var request : requests) {
        // Ensure 401 when there is no JWT at all.
        Fluent
            .of(restAssuredJson(jsonBody).auth().none())
            .map(request)
            .get()
            .then()
            .statusCode(401)
            .body("error", containsString("invalid JWT"));

        // Ensure 403 for authorization failure when scopes are missing.
        // This is the baseline to ensure authorization isn't granted without the scopes.
        Fluent
            .of(restAssuredJson(jsonBody).auth().oauth2(createJwtWithScope("invalid-test-scope")))
            .map(request)
            .get()
            .then()
            .statusCode(403)
            .body("error", containsString("Access is denied"));

        // Ensure we get past authorization when each scope is included individually.
        // Note that errors like 400 and 404 are fine because it still indicates authZ worked.
        for (var scope : scopes) {
          Fluent
              .of(restAssuredJson(jsonBody).auth().oauth2(createJwtWithScope(scope)))
              .map(request)
              .get()
              .then()
              .statusCode(anyOf(is(200), is(201), is(204), is(400), is(404)));
        }
      }
    }
  }
}
