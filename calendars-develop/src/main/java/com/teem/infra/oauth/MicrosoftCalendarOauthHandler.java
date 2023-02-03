package com.UoU.infra.oauth;

import com.UoU.core.auth.AuthMethod;
import com.UoU.core.auth.OauthConfig;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Microsoft OAuth handler for calendar access.
 */
@Service
public class MicrosoftCalendarOauthHandler extends MicrosoftBaseOauthHandler {

  private static final List<AuthMethod> METHODS = List.of(AuthMethod.MS_OAUTH_SA);

  /**
   * Scopes for the authorize step (get authorization code).
   */
  private static final List<String> AUTH_SCOPES = List.of(
      "https://outlook.office365.com/EAS.AccessAsUser.All",
      "https://outlook.office365.com/EWS.AccessAsUser.All",
      "https://graph.microsoft.com/Calendars.ReadWrite.Shared",
      "User.Read",
      "openid",
      "profile",
      "email",
      "offline_access");


  /**
   * Scopes for the token exchange step (get refresh and access tokens).
   *
   * <p>See {@link MicrosoftBaseOauthHandler} for details about token scopes.
   */
  private static final List<String> TOKEN_SCOPES = List.of(
      "User.Read",
      "openid",
      "profile",
      "email",
      "offline_access");

  public MicrosoftCalendarOauthHandler(OauthConfig oauthConfig, OauthClient oauthClient) {
    super(oauthConfig, oauthClient, METHODS, AUTH_SCOPES, TOKEN_SCOPES);
  }
}
