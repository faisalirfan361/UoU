package com.UoU._integration.api.v1;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.UoU._fakes.AuthServiceSpy;
import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU._integration.api.BaseApiIntegrationTest;
import com.UoU.core.Fluent;
import com.UoU.core.accounts.ServiceAccount;
import com.UoU.core.accounts.ServiceAccountCreateRequest;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.auth.AuthMethod;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

// TODO: This class should be combined with unauthenticated/AuthConnectControllerTests.java.
public class AuthConnectControllerTests extends BaseApiIntegrationTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Getter
  private final String basePath = "/v1/auth/connect";

  @Autowired
  private AuthServiceSpy authServiceSpy;

  @AfterEach
  private void tearDown() {
    reset(authServiceSpy);
  }

  @Test
  @SuppressWarnings("unchecked")
  void connectForm_shouldWorkForGoogleServiceAccountWithNewEmail() {
    val authCode = redisHelper.createAuthCode(orgId);
    val email = TestData.email();
    val authData = Map.of(
        "json", writeJson(createGoogleJson(email, Map.of("value", "test"))));

    val serviceAccount = doFormPostForServiceAccount(
        AuthMethod.GOOGLE_SA, authCode, authData);
    val authInfo = dbHelper.getServiceAccountRepo().getAuthInfo(serviceAccount.id());

    assertThat(serviceAccount.email())
        .as("Service account should have been created with expected email.")
        .isEqualTo(email);
    assertThat(((Map<String, String>) authInfo.settings().get("service_account_json"))
        .get("value"))
        .isEqualTo("test");
    assertThat(redisHelper.getAuthCodeRepo().tryGet(authCode))
        .as("Auth code should have been deleted.")
        .isEmpty();
  }

  @Test
  @SuppressWarnings("unchecked")
  void connectForm_shouldWorkForGoogleServiceAccountWithExistingEmail() {
    val authCode = redisHelper.createAuthCode(orgId);
    val email = TestData.email();
    val authData = Map.of(
        "json", writeJson(createGoogleJson(email, Map.of("update-value", "test"))));

    // Create service account so it exists before auth.
    val id = TestData.serviceAccountId();
    dbHelper.getServiceAccountRepo().create(ServiceAccountCreateRequest.builder()
        .id(id)
        .orgId(orgId)
        .email(email)
        .settings(Map.of("test", "test"))
        .authMethod(AuthMethod.GOOGLE_SA)
        .build());

    val serviceAccount = doFormPostForServiceAccount(
        AuthMethod.GOOGLE_SA, authCode, authData);
    val authInfo = dbHelper.getServiceAccountRepo().getAuthInfo(serviceAccount.id());

    assertThat(serviceAccount.id()).isEqualTo(id);
    assertThat(serviceAccount.email()).isEqualTo(email);
    assertThat(serviceAccount.updatedAt())
        .as("Service account should have been updated.")
        .isAfter(serviceAccount.createdAt());
    assertThat(((Map<String, String>) authInfo.settings().get("service_account_json"))
        .get("update-value"))
        .isEqualTo("test");
    assertThat(redisHelper.getAuthCodeRepo().tryGet(authCode))
        .as("Auth code should have been deleted.")
        .isEmpty();
  }

  @Test
  void connectForm_shouldWorkWithoutRedirectUrl() {
    val authCode = UUID.randomUUID();
    redisHelper.getAuthCodeRepo().create(ModelBuilders
        .authCodeCreateRequestWithTestData()
        .code(authCode)
        .redirectUri(null)
        .build());

    val email = TestData.email();
    val authData = Map.of(
        "json", writeJson(createGoogleJson(email, Map.of())));

    restAssuredUnauthenticated()
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)
        .formParams(authData)
        .redirects().follow(false)
        .post("/{method}/{code}", AuthMethod.GOOGLE_SA.getValue(), authCode.toString())
        .then()
        .statusCode(302)
        .header("location", endsWith("/v1/auth/connect/success"));
  }

  @Test
  void connectForm_shouldReturnFriendly400ForInvalidAuthData() {
    restAssuredUnauthenticated()
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)
        .formParam("invalid", "invalid")
        .post("/{method}/{code}", AuthMethod.GOOGLE_SA.getValue(), TestData.uuidString())
        .then()
        .statusCode(400)
        .contentType(ContentType.HTML)
        .body(containsString("Invalid auth request"));
  }

  @Test
  void connectForm_should500ForAuthServiceException() {
    doThrow(new RuntimeException("ERROR!!!!!!!"))
        .when(authServiceSpy).handleDirectionSubmissionAuth(any(), any(), any());

    restAssuredUnauthenticated()
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)
        .formParam("test", true)
        .post("/{method}/{code}", AuthMethod.GOOGLE_SA.getValue(), TestData.uuidString())
        .then()
        .statusCode(500)
        .contentType(ContentType.HTML)
        .body(containsString("Error"));
  }

  @Test
  @SuppressWarnings("unchecked")
  void connectJson_shouldWorkForGoogleServiceAccountWithNewEmail() {
    val authCode = redisHelper.createAuthCode(orgId);
    val email = TestData.email();
    val authData = createGoogleJson(email, Map.of("value", "test"));

    val serviceAccount = doJsonPostForServiceAccount(
        AuthMethod.GOOGLE_SA, authCode, authData);
    val authInfo = dbHelper.getServiceAccountRepo().getAuthInfo(serviceAccount.id());

    assertThat(serviceAccount.email())
        .as("Service account should have been created with expected email.")
        .isEqualTo(email);
    assertThat(((Map<String, String>) authInfo.settings().get("service_account_json"))
        .get("value"))
        .isEqualTo("test");
    assertThat(redisHelper.getAuthCodeRepo().tryGet(authCode))
        .as("Auth code should have been deleted.")
        .isEmpty();
  }

  @Test
  @SuppressWarnings("unchecked")
  void connectJson_shouldWorkForGoogleServiceAccountWithExistingEmail() {
    val authCode = redisHelper.createAuthCode(orgId);
    val email = TestData.email();
    val authData = createGoogleJson(email, Map.of("update-value", "test"));

    // Create service account so it exists before auth.
    val id = TestData.serviceAccountId();
    dbHelper.getServiceAccountRepo().create(ServiceAccountCreateRequest.builder()
        .id(id)
        .orgId(orgId)
        .email(email)
        .settings(Map.of("test", "test"))
        .authMethod(AuthMethod.GOOGLE_SA)
        .build());

    val serviceAccount = doJsonPostForServiceAccount(
        AuthMethod.GOOGLE_SA, authCode, authData);
    val authInfo = dbHelper.getServiceAccountRepo().getAuthInfo(serviceAccount.id());

    assertThat(serviceAccount.id()).isEqualTo(id);
    assertThat(serviceAccount.email()).isEqualTo(email);
    assertThat(serviceAccount.updatedAt())
        .as("Service account should have been updated.")
        .isAfter(serviceAccount.createdAt());
    assertThat(((Map<String, String>) authInfo.settings().get("service_account_json"))
        .get("update-value"))
        .isEqualTo("test");
    assertThat(redisHelper.getAuthCodeRepo().tryGet(authCode))
        .as("Auth code should have been deleted.")
        .isEmpty();
  }

  @Test
  void connectJson_shouldReturnFriendly400ForInvalidAuthData() {
    val authCode = redisHelper.createAuthCode(orgId);
    restAssuredJson(Map.of("invalid", "invalid"))
        .post("/{method}/{code}", AuthMethod.GOOGLE_SA.getValue(), authCode.toString())
        .then()
        .statusCode(400)
        .contentType(ContentType.JSON)
        .body("error", containsString("service account JSON"));
  }

  @Test
  void connectJson_should500ForAuthServiceException() {
    doThrow(new RuntimeException("ERROR!!!!!!!"))
        .when(authServiceSpy).handleDirectionSubmissionAuth(any(), any(), any());

    restAssuredJson(Map.of("test", true))
        .post("/{method}/{code}", AuthMethod.GOOGLE_SA.getValue(), TestData.uuidString())
        .then()
        .statusCode(500)
        .contentType(ContentType.JSON)
        .body("error", containsString("Error"));
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

  private ServiceAccount doFormPostForServiceAccount(
      AuthMethod method, UUID authCode, Map<String, String> formData) {

    val redirectUrl = restAssuredUnauthenticated()
        .redirects().follow(false)
        .contentType(APPLICATION_FORM_URLENCODED_VALUE)
        .formParams(formData)
        .post("/{method}/{code}", method.getValue(), authCode.toString())
        .then()
        .statusCode(302)
        .extract()
        .header("Location");

    val idString = redirectUrl.split("calendarsApiServiceAccountId=")[1];
    val id = new ServiceAccountId(UUID.fromString(idString));
    return dbHelper.getServiceAccountRepo().get(id);
  }

  private ServiceAccount doJsonPostForServiceAccount(
      AuthMethod method, UUID authCode, Map<String, String> jsonData) {

    val id = restAssuredJson(jsonData)
        .auth().none()
        .post("/{method}/{code}", method.getValue(), authCode.toString())
        .then()
        .statusCode(200)
        .extract()
        .jsonPath()
        .getUUID("id");

    return dbHelper.getServiceAccountRepo().get(new ServiceAccountId(id));
  }

  private static Map<String, String> createGoogleJson(String email, Map<String, String> data) {
    return Fluent
        .of(new HashMap<String, String>())
        .also(x -> x.putAll(Map.of(
            "type", "service_account",
            "project_id", "test",
            "private_key_id", "test",
            "private_key", "test",
            "client_email", email,
            "client_id", "test",
            "auth_uri", "https://example.com",
            "token_uri", "https://example.com",
            "auth_provider_x509_cert_url", "https://example.com",
            "client_x509_cert_url", "https://example.com")))
        .also(x -> x.putAll(data))
        .get();
  }

  @SneakyThrows
  private static String writeJson(Map<String, ?> data) {
    return MAPPER.writeValueAsString(data);
  }
}
