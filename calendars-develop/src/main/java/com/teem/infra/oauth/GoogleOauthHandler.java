package com.UoU.infra.oauth;

import com.UoU.core.auth.AuthMethod;
import com.UoU.core.auth.OauthConfig;
import com.UoU.core.auth.OauthHandler;
import com.UoU.core.auth.OauthResult;
import com.UoU.core.auth.OauthState;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@AllArgsConstructor
public class GoogleOauthHandler implements OauthHandler {
  private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
  private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
  private static final String SCOPES = String.join(" ",
      "openid",
      "https://www.googleapis.com/auth/userinfo.email",
      "https://www.googleapis.com/auth/userinfo.profile",
      "https://www.googleapis.com/auth/calendar");

  private final OauthConfig oauthConfig;
  private final OauthClient oauthClient;

  @Override
  public List<AuthMethod> methods() {
    return List.of(AuthMethod.GOOGLE_OAUTH);
  }

  @Override
  public String getRedirectUrl(OauthState state) {
    return UriComponentsBuilder
        .fromUriString(AUTH_URL)
        .queryParam(OAuth2ParameterNames.RESPONSE_TYPE, "code")
        .queryParam(OAuth2ParameterNames.CLIENT_ID, oauthConfig.google().clientId())
        .queryParam(OAuth2ParameterNames.REDIRECT_URI, oauthConfig.redirectUri())
        .queryParam(OAuth2ParameterNames.SCOPE, SCOPES)
        .queryParam("access_type", "offline") // custom google param
        .queryParam(OAuth2ParameterNames.STATE, state.encode())
        .queryParam("prompt", "consent select_account") // custom google param
        .toUriString();
  }

  @Override
  public OauthResult handleAuthorizationCode(String code) {
    return oauthClient.exchangeAuthorizationCode(TOKEN_URL, oauthConfig.google(), code);
  }
}
