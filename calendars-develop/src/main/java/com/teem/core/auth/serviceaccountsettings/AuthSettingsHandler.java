package com.UoU.core.auth.serviceaccountsettings;

import com.UoU.core.SecretString;
import com.UoU.core.auth.AuthInput;
import com.UoU.core.auth.AuthMethod;
import java.util.Map;
import java.util.Optional;

/**
 * Creates and handles parsing for service account settings.
 *
 * <p>This creates auth settings that can be stored with a service account and later used to auth
 * associated subaccounts.
 */
public interface AuthSettingsHandler {

  /**
   * The auth method this handler should be used for.
   */
  AuthMethod authMethod();

  /**
   * Creates auth settings for a service account based on user auth input.
   */
  AuthSettings createSettings(AuthInput authInput);

  /**
   * Gets a refresh token from settings created via {@link #createSettings(AuthInput)}.
   *
   * <p>This will only return a value for certain OAuth auth methods, since some methods will
   * not use a refresh token.
   */
  default Optional<SecretString> getRefreshTokenFromSettings(Map<String, Object> settings) {
    return Optional.empty();
  }
}
