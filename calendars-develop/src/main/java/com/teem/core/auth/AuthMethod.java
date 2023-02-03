package com.UoU.core.auth;

import com.UoU.core.accounts.Provider;
import java.util.Arrays;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthMethod {
  // Calendar auth:
  INTERNAL(
      "internal",
      Provider.INTERNAL,
      DataType.CALENDAR,
      Flow.None,
      false),
  MS_OAUTH_SA(
      "ms-oauth-sa",
      Provider.MICROSOFT,
      DataType.CALENDAR,
      Flow.OauthAuthorizationCode,
      true),
  GOOGLE_OAUTH(
      "google-oauth",
      Provider.GOOGLE,
      DataType.CALENDAR,
      Flow.OauthAuthorizationCode,
      false),
  GOOGLE_SA(
      "google-sa",
      Provider.GOOGLE,
      DataType.CALENDAR,
      Flow.DirectSubmission,
      true),

  // Conferencing auth:
  CONF_TEAMS_OAUTH(
      "conf-teams-oauth",
      Provider.MICROSOFT,
      DataType.CONFERENCING,
      Flow.OauthAuthorizationCode,
      false);

  private final String value;
  private final Provider provider;
  private final DataType dataType;
  private final Flow flow;
  private final boolean isForServiceAccounts;

  public static Optional<AuthMethod> byStringValue(String value) {
    return Arrays.stream(AuthMethod.values()).filter(x -> x.getValue().equals(value)).findFirst();
  }

  /**
   * The type of data the auth method grants access to.
   */
  public enum DataType {
    CALENDAR, CONFERENCING
  }

  /**
   * The user/data flow used for the method, which determines how we gather and process auth data.
   */
  public enum Flow {

    /**
     * User auth is not required (for internal calendars only).
     */
    None,

    /**
     * User completes OAuth authorization code flow with an OAuth provider.
     *
     * <p>This is for the standard 3-legged OAuth flow where we redirect the user to the OAuth
     * provider and handle the result. We don't gather credentials directly from the user.
     */
    OauthAuthorizationCode,

    /**
     * User directly submits auth credentials to us (which may be non-OAuth or 2-legged OAuth).
     *
     * <p>For 2-legged OAuth, like client credentials grant, this flow should be used because we
     * gather the credentials directly from a user. Even though the credentials may be used for
     * OAuth later on by Nylas, we don't actually do anything OAuth-related on our side. Therefore,
     * 2-legged OAuth works just like username/password or any other method where we gather creds
     * from a user and pass them to Nylas.
     */
    DirectSubmission,
  }
}
