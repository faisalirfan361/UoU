package com.UoU._fakes.nylas;

import com.UoU._helpers.TestData;
import com.UoU.core.SecretString;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.ServiceAccountAuthInfo;
import com.UoU.core.auth.AuthMethod;
import com.UoU.core.nylas.auth.NylasAuthResult;
import com.UoU.core.nylas.auth.NylasAuthService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.val;

/**
 * Fake Nylas auth service that allows preconfiguring fake account ids by email.
 */
public class FakeNylasAuthService extends NylasAuthService {
  private static final Map<String, AccountId> ACCOUNTS = new HashMap<>();
  private static final Map<String, Exception> EXCEPTIONS = new HashMap<>();

  public FakeNylasAuthService() {
    super(TestData.oauthConfig(), null);
  }

  @Override
  public NylasAuthResult authSubaccount(
      ServiceAccountAuthInfo authInfo, String name, String email) {
    return fakeAuth(email);
  }

  @Override
  public NylasAuthResult authAccount(
      AuthMethod authMethod, SecretString refreshToken, String name, String email) {
    return fakeAuth(email);
  }

  @Override
  public NylasAuthResult authVirtualAccount(String name, String email, String timezone) {
    return fakeAuth(email);
  }

  /**
   * Adds a specific accountId that will be returned in NylasAuthResult when the email is authed.
   */
  public static void fakeAccountIdForEmail(String email, AccountId accountId) {
    if (ACCOUNTS.containsKey(email)) {
      throw new IllegalArgumentException(email + " already exists in faked account ids.");
    }
    ACCOUNTS.put(email, accountId);
  }

  /**
   * Adds an exception to be thrown when the email is authed.
   */
  public static void fakeExceptionForEmail(String email, Exception exception) {
    if (EXCEPTIONS.containsKey(email)) {
      throw new IllegalArgumentException(email + " already exists in faked exceptions.");
    }
    EXCEPTIONS.put(email, exception);
  }

  @SneakyThrows
  private NylasAuthResult fakeAuth(String email) {
    val exception = EXCEPTIONS.get(email);
    if (exception != null) {
      throw exception;
    }

    val accountId = Optional.ofNullable(ACCOUNTS.get(email)).orElseGet(TestData::accountId);
    return new NylasAuthResult(accountId, new SecretString("test-" + TestData.uuidString()));
  }
}
