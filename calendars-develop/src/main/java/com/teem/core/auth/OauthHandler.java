package com.UoU.core.auth;

import com.UoU.core.SecretString;
import java.util.List;

/**
 * Handles low-level OAuth operations for specific auth method(s).
 */
public interface OauthHandler {

  /**
   * Returns the auth methods this handler works for.
   */
  List<AuthMethod> methods();

  /**
   * Gets the redirect URL that should be used to initiate OAuth.
   *
   * <p>This should usually use the authorization code grant (response_type=code) so that the
   * callback can be handled by {@link #handleAuthorizationCode(String)}.
   */
  String getRedirectUrl(OauthState state);

  /**
   * Completes the authorization code grant process by exchanging the code for tokens.
   */
  OauthResult handleAuthorizationCode(String code);

  /**
   * Returns whether the handler supports using a refresh token to get new tokens.
   *
   * <p>If this returns true, {@link #refresh(SecretString)} must be implemented.
   */
  default boolean supportsRefresh() {
    return false;
  }

  /**
   * Uses an existing refresh token to get new tokens, including a new refresh token.
   *
   * <p>If {@link #supportsRefresh()} returns true, this must be implemented.
   */
  default OauthResult refresh(SecretString refreshToken) {
    throw new UnsupportedOperationException("OAuthHandler does not support refresh");
  }
}
