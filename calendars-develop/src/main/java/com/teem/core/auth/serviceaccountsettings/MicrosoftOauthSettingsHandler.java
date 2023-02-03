package com.UoU.core.auth.serviceaccountsettings;

import com.UoU.core.SecretString;
import com.UoU.core.auth.AuthInput;
import com.UoU.core.auth.AuthMethod;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Creates and handles parsing for Microsoft service account settings (MS_OAUTH_SA auth method).
 */
@Service
public class MicrosoftOauthSettingsHandler implements AuthSettingsHandler {

  private static final String KEY_REFRESH_TOKEN = "microsoft_refresh_token";
  private static final int SETTINGS_EXPIRATION_DAYS = 88;

  @Override
  public AuthMethod authMethod() {
    return AuthMethod.MS_OAUTH_SA;
  }

  /**
   * Creates service account settings that expire in 88 days because the MS refresh token expires.
   *
   * <p>MS refresh tokens expire after 90 days. After expiration, the settings with refresh token
   * will need to be replaced. Otherwise, the token would be expired the next time someone tries to
   * use the service account to connect a subaccount. This uses an expiration of 88 days to allow
   * some extra time for the token replacement to happen.
   *
   * <p>The MS docs are confusing on token expiration, and on forums some people say the inactive
   * timeout is 14 days and can be extended to 90 days as long as the token is used. Some people say
   * the timeout is configurable, and others say it's not. The latest MS docs seem to say the total
   * lifetime is 90 days regardless of use and that it's not configurable: "The default lifetime
   * for the refresh tokens is 24 hours for single page apps and 90 days for all other scenarios.
   * Refresh tokens replace themselves with a fresh token upon every use. The Microsoft identity
   * platform doesn't revoke old refresh tokens when used to fetch new access tokens. Securely
   * delete the old refresh token after acquiring a new one. [much later in same doc]... You can't
   * configure the lifetime of a refresh token. You can't reduce or lengthen their lifetime."
   * <a href="https://docs.microsoft.com/en-us/azure/active-directory/develop/refresh-tokens">See MS docs</a>
   */
  @Override
  public AuthSettings createSettings(AuthInput authInput) {
    return new AuthSettings(
        authInput.getOauthResult().email(),
        Map.of(KEY_REFRESH_TOKEN, authInput.getOauthResult().refreshToken().value()),
        Instant.now().plus(SETTINGS_EXPIRATION_DAYS, ChronoUnit.DAYS));
  }

  @Override
  public Optional<SecretString> getRefreshTokenFromSettings(Map<String, Object> settings) {
    return Optional
        .ofNullable(settings)
        .flatMap(x -> Optional.ofNullable(x.get(KEY_REFRESH_TOKEN)))
        .map(x -> new SecretString((String) x));
  }
}
