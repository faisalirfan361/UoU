package com.UoU.core.auth;

import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * Holds auth input that is either an OAuth result or directly-submitted auth data (credentials).
 *
 * <p>Auth input can be either {@link OauthResult} or directly-submitted auth data, but not both.
 * For OAuth auth code flow, a user goes through the flow, and we get an OAuthResult at the end.
 * For non-OAuth (or 2-legged OAuth) auth methods, a user directly submits auth credentials to us.
 * In both cases, once we have this input, the rest of the auth process can be handled in mostly the
 * same way. This class is an abstraction that allows common code to handle both scenarios.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthInput {
  private final OauthResult oauthResult;
  private final Map<String, Object> authData;

  public boolean hasOauthResult() {
    return oauthResult != null;
  }

  /**
   * Gets the OAuth result or throws an exception if it's missing.
   */
  public OauthResult getOauthResult() {
    if (!hasOauthResult()) {
      throw new IllegalStateException("AuthInput does not have OAuth result");
    }
    return oauthResult;
  }

  public boolean hasDirectlySubmittedAuthData() {
    return authData != null;
  }

  /**
   * Gets the user-submitted auth data or throws an exception if it's missing.
   */
  public Map<String, Object> getDirectlySubmittedAuthData() {
    if (!hasDirectlySubmittedAuthData()) {
      throw new IllegalStateException("AuthInput does not have directly submitted auth data");
    }
    return authData;
  }

  public static AuthInput ofOauthResult(@NonNull OauthResult oauthResult) {
    return new AuthInput(oauthResult, null);
  }

  public static AuthInput ofDirectlySubmittedAuthData(@NonNull Map<String, Object> authData) {
    return new AuthInput(null, authData);
  }
}
