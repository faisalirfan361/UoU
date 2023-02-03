package com.UoU._fakes.oauth;

import com.UoU._helpers.TestData;
import com.UoU.core.SecretString;
import com.UoU.core.auth.OauthConfig;
import com.UoU.core.auth.OauthException;
import com.UoU.core.auth.OauthResult;
import com.UoU.infra.oauth.OauthClient;
import java.time.Instant;
import java.util.Map;

/**
 * Fakes OAuth calls for testing.
 *
 * <p>For authorization, this fakes authorization calls by returning success for any code except
 * {@link #getFailureCode()}. You can get the email based on auth code that will be returned via
 * {@link #getFakeEmail(String)} which is deterministic per test run.
 *
 * <p>For token refresh, this fakes calls by returning success for any refresh token except
 * {@link #getFailureRefreshToken()}. You can get the email based on refresh token that will be
 * returned via {@link #getFakeEmail(SecretString)} which is deterministic per test run.
 */
public class FakeOauthClient extends OauthClient {
  private static final String UNIQUE_SEED = TestData.uuidString().replace("-", "");
  private static final String FAILURE_CODE = "fail";
  private static final SecretString FAILURE_REFRESH_TOKEN = new SecretString("fail");

  public FakeOauthClient() {
    super(null, null);
  }

  @Override
  public OauthResult exchangeAuthorizationCode(
      String tokenUrl,
      OauthConfig.OauthCredentials credentials,
      String code) {
    return exchangeAuthorizationCode(tokenUrl, credentials, code, Map.of());
  }

  @Override
  public OauthResult exchangeAuthorizationCode(
      String tokenUrl,
      OauthConfig.OauthCredentials credentials,
      String code,
      Map<String, String> extraParams) {

    if (code.equals(FAILURE_CODE)) {
      throw new OauthException("Test oauth failure");
    }

    return new OauthResult(
        "fake", getFakeEmail(code), new SecretString("refresh"), new SecretString("access"), 100);
  }

  @Override
  public OauthResult exchangeRefreshToken(
      String tokenUrl,
      OauthConfig.OauthCredentials credentials,
      SecretString refreshToken,
      Map<String, String> extraParams) {

    if (refreshToken.equals(FAILURE_REFRESH_TOKEN)) {
      throw new OauthException("Test oauth refresh failure");
    }

    return new OauthResult(
        "fake",
        getFakeEmail(refreshToken),
        refreshToken,
        new SecretString("access-refreshed-" + Instant.now().getEpochSecond()),
        100);
  }

  /**
   * Gets the fake email that will be used for the give code (deterministic per test run).
   */
  public static String getFakeEmail(String code) {
    return code.replaceAll("[^a-zA-Z0-9.]", "") + "@" + UNIQUE_SEED + ".example.com";
  }

  /**
   * Gets the fake email that will be used for the give refresh token (deterministic per test run).
   */
  public static String getFakeEmail(SecretString refreshToken) {
    return refreshToken.value().replaceAll("[^a-zA-Z0-9.]", "")
           + "@" + UNIQUE_SEED + ".example.com";
  }

  /**
   * Gets the code that will trigger an exception in OAuth handling based on authorization code.
   */
  public static String getFailureCode() {
    return FAILURE_CODE;
  }

  /**
   * Gets the refresh token that will trigger an exception in OAuth handling during refresh.
   */
  public static SecretString getFailureRefreshToken() {
    return FAILURE_REFRESH_TOKEN;
  }
}
