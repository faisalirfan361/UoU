package com.UoU.infra.oauth;

import com.UoU.core.auth.AuthMethod;
import com.UoU.core.auth.OauthConfig;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Microsoft OAuth handler for Teams conferencing access.
 */
@Service
public class MicrosoftTeamsOauthHandler extends MicrosoftBaseOauthHandler {

  private static final List<AuthMethod> METHODS = List.of(AuthMethod.CONF_TEAMS_OAUTH);

  /**
   * Scopes for the both the authorize and token steps.
   *
   * <p>See {@link MicrosoftBaseOauthHandler} for details about token scopes.
   */
  private static final List<String> SCOPES = List.of(
      "https://graph.microsoft.com/OnlineMeetings.ReadWrite",
      "User.Read",
      "openid",
      "profile",
      "email",
      "offline_access");

  public MicrosoftTeamsOauthHandler(OauthConfig oauthConfig, OauthClient oauthClient) {
    super(oauthConfig, oauthClient, METHODS, SCOPES, SCOPES);
  }
}
