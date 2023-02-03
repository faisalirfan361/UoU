package com.UoU.core.nylas.auth;

import com.nylas.AccessToken;
import com.nylas.ProviderSettings;
import com.nylas.RequestFailedException;
import com.nylas.Scope;
import com.UoU.core.SecretString;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.ServiceAccountAuthInfo;
import com.UoU.core.auth.AuthMethod;
import com.UoU.core.auth.OauthConfig;
import com.UoU.core.nylas.NylasClientFactory;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Handles the Nylas portion of our auth flows using Nylas native authentication.
 *
 * <p>This wraps the Nylas SDK auth calls to provide common setup and error handling.
 */
@Service
@AllArgsConstructor
@Slf4j
public class NylasAuthService {
  private static final Scope[] NYLAS_SCOPES = new Scope[]{Scope.CALENDAR};

  private final OauthConfig oauthConfig;
  private final NylasClientFactory nylasClientFactory;

  /**
   * Auths a service account subaccount in Nylas using native authentication.
   *
   * <p>This throws NylasAuthException for Nylas auth errors (401, 403, auth_error, etc.) that
   * indicate bad input where the user could fix the issue and try again. Other errors are thrown
   * as per usual with the Nylas SDK, like RequestFailedException.
   */
  public NylasAuthResult authSubaccount(
      ServiceAccountAuthInfo authInfo, String name, String email) {

    val providerSettings = switch (authInfo.authMethod()) {
      case MS_OAUTH_SA -> new MicrosoftExchangeOauthServiceAccountProviderSettings()
          .microsoftClientId(oauthConfig.microsoft().clientId())
          .microsoftClientSecret(oauthConfig.microsoft().clientSecret().value())
          .redirectUri(oauthConfig.redirectUri())
          .addAll(authInfo.settings());

      case GOOGLE_SA -> new GoogleServiceAccountProviderSettings()
          .addAll(authInfo.settings());

      default -> throw new IllegalArgumentException("Invalid service account auth method");
    };

    return auth(name, email, providerSettings);
  }

  /**
   * Auths a normal account in Nylas using native authentication.
   *
   * <p>AuthMethod must be a non-service account method.
   *
   * <p>This throws NylasAuthException for Nylas auth errors (401, 403, auth_error, etc.) that
   * indicate bad input where the user could fix the issue and try again. Other errors are thrown
   * as per usual with the Nylas SDK, like RequestFailedException.
   */
  public NylasAuthResult authAccount(
      AuthMethod authMethod, SecretString refreshToken, String name, String email) {

    val providerSettings = switch (authMethod) {
      case GOOGLE_OAUTH -> ProviderSettings.google()
          .googleClientId(oauthConfig.google().clientId())
          .googleClientSecret(oauthConfig.google().clientSecret().value())
          .googleRefreshToken(refreshToken.value());

      default -> throw new IllegalArgumentException("Invalid auth method");
    };

    return auth(name, email, providerSettings);
  }

  /**
   * Auths a virtual account in Nylas, which does not sync to any provider (MS/Google).
   *
   * <p>See <a href="https://developer.nylas.com/docs/connectivity/calendar/virtual-calendar/">Nylas docs</a>
   */
  public NylasAuthResult authVirtualAccount(String name, String email, String timezone) {
    return auth(name, email, VirtualAccountProviderSettings.INSTANCE);
  }

  @SneakyThrows
  private NylasAuthResult auth(String name, String email, ProviderSettings providerSettings) {
    val client = nylasClientFactory.createApplicationClient();

    // Get authorization code:
    String code;
    try {
      code = client
          .nativeAuthentication()
          .authRequest()
          .name(name)
          .emailAddress(email)
          .providerSettings(providerSettings)
          .scopes(NYLAS_SCOPES)
          .execute();
    } catch (RequestFailedException ex) {
      log.info("Nylas auth request failed for {}", email, ex);
      throw Exceptions.get("Account auth failed during initial authorization.", ex);
    }

    // Exchange code for token:
    AccessToken token;
    try {
      token = client.nativeAuthentication().fetchToken(code);
    } catch (RequestFailedException ex) {
      log.info("Nylas auth token exchange failed for {}", email, ex);
      throw Exceptions.get("Account auth failed during token exchange.", ex);
    }

    try {
      client.accounts().revokeAllTokensForAccount(token.getAccountId(), token.getAccessToken());
    } catch (RequestFailedException ex) {
      log.warn("Nylas Revoke All tokens failed for {}", email, ex);
    }

    return new NylasAuthResult(
        new AccountId(token.getAccountId()),
        new SecretString(token.getAccessToken()));
  }

  /**
   * Private helper to handle Nylas exceptions in a common way.
   */
  private static class Exceptions {
    private static final String TYPE_AUTH_ERROR = "auth_error";
    private static final String MSG_INVALID_EMAIL = "No useable credentials";
    private static final String MSG_UNGRANTED_SCOPES = "scopes don't match";

    private static Exception get(String message, RequestFailedException ex) {
      val nylasMsg = ex.getErrorMessage();
      val isAuthError = TYPE_AUTH_ERROR.equals(ex.getErrorType());

      // 403: Occurs when permissions are wrong in the provider:
      // "This service account does not have the permission to impersonate the requested user."
      if (ex.getStatusCode() == 403) {
        val extra = " Check the service account permissions in the calendar provider. Error: ";
        return new NylasAuthException(message + extra + nylasMsg, ex);
      }

      // 500 auth_error is used when the email is invalid for the provider domain/tenant:
      // "An unexpected error occurred during authentication: No useable credentials"
      // This error message is really confusing, so we'll add some extra detail for this case.
      if (isAuthError && nylasMsg.contains(MSG_INVALID_EMAIL)) {
        val extra = " Check that the email is valid for the calendar provider. Error: ";
        return new NylasAuthException(message + extra + nylasMsg, ex);
      }

      // Authorized scopes error, when user doesn't approve all the OAuth scopes:
      // "Authorized scopes don't match provided scopes during token validation..."
      if (isAuthError && nylasMsg.contains(MSG_UNGRANTED_SCOPES)) {
        val extra = " User did not grant required access to Google account. Please try again.";
        return new NylasAuthException(message + extra, ex);
      }

      // Handle other auth errors and bad input errors by just including the Nylas message:
      if (isAuthError || ex.getStatusCode() == 400 || ex.getStatusCode() == 401) {
        return new NylasAuthException(message + " Error: " + nylasMsg, ex);
      }

      // For unknown failures, return original exception, not NylasAuthError because that's supposed
      // to represent a known issue with bad input or permissions that the user could fix.
      return ex;
    }
  }
}
