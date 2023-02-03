package com.UoU.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.UoU._helpers.TestData;
import com.UoU.core.auth.AuthCode;
import com.UoU.core.auth.AuthResult;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AuthConnectHelperTests {
  private static final AuthConnectHelper HELPER = new AuthConnectHelper();

  @ParameterizedTest
  @ValueSource(strings = {
      "http://example.com",
      "https://example.com/",
      "https://example.com/?a=b",
      "https://example.com/?a=b#test",
  })
  void getAuthSuccessRedirectUri_shouldParseValidUris(String uri) {
    val authCode = new AuthCode(UUID.randomUUID(), TestData.orgId(), uri);
    val accountAuthResult = new AuthResult(authCode, TestData.accountId());
    val serviceAccountAuthResult = new AuthResult(authCode, TestData.serviceAccountId());

    val accountResult = HELPER.getAuthSuccessRedirectUri(accountAuthResult);
    val serviceAccountResult = HELPER.getAuthSuccessRedirectUri(serviceAccountAuthResult);

    assertThat(accountResult).hasValueSatisfying(x -> assertThat(x)
        .startsWith("http")
        .contains("calendarsApiAccountId=" + accountAuthResult.id()));
    assertThat(serviceAccountResult).hasValueSatisfying(x -> assertThat(x)
        .startsWith("http")
        .contains("calendarsApiServiceAccountId=" + serviceAccountAuthResult.id()));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      " ",
      "invalid",
      "/invalid",
      "//invalid",
      "ftp://invalid.com",
  })
  void getAuthSuccessRedirectUri_shouldReturnEmptyForInvalidUris(String uri) {
    val authResult = new AuthResult(
        new AuthCode(UUID.randomUUID(), TestData.orgId(), uri),
        TestData.serviceAccountId());

    val result = HELPER.getAuthSuccessRedirectUri(authResult);

    assertThat(result).isEmpty();
  }
}
