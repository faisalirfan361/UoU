package com.UoU.infra.oauth;

import com.UoU.core.SecretString;
import com.UoU.core.auth.AuthMethod;
import com.UoU.core.auth.OauthConfig;
import com.UoU.core.auth.OauthHandler;
import com.UoU.core.auth.OauthResult;
import com.UoU.core.auth.OauthState;
import java.util.List;
import java.util.Map;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Base Microsoft OAuth handler, allowing implementations to vary methods and scopes.
 */
abstract class MicrosoftBaseOauthHandler implements OauthHandler {

  /**
   * URL for the authorize step (get authorization code).
   */
  private static final String AUTH_URL =
      "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";

  /**
   * URL for the token exchange step (get refresh and access tokens).
   */
  private static final String TOKEN_URL =
      "https://login.microsoftonline.com/common/oauth2/v2.0/token";

  private final OauthConfig oauthConfig;
  private final OauthClient oauthClient;
  private final List<AuthMethod> methods;
  private final String authScopes;
  private final String tokenScopes;

  /**
   * Creates an MS OAuth handler.
   *
   * <p>Token scopes are an MS extension to oauth and determine the single resource-type the result
   * access token will be valid for. The refresh token can be used to fetch an access token for any
   * single resource authorized in the first step. Since we're not actually using the access token,
   * just the refresh token, we'll just request a minimal access token with the openid scopes and
   * User.Read (since we have to provide at least one resource scope).
   *
   * <p>From MS: "This parameter is a Microsoft extension to the authorization code flow, intended
   * to allow apps to declare the resource they want the token for during token redemption."
   * <a href="https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-auth-code-flow#request-an-access-token-with-a-client_secret">See docs.</a>
   *
   * @param oauthConfig Config object
   * @param oauthClient Helper client for OAuth HTTP calls
   * @param methods     Methods the handler works for
   * @param authScopes  Scopes to use for the auth code step
   * @param tokenScopes Scopes to use for the token exchange step (see below).
   */
  protected MicrosoftBaseOauthHandler(
      OauthConfig oauthConfig,
      OauthClient oauthClient,
      List<AuthMethod> methods,
      List<String> authScopes,
      List<String> tokenScopes) {
    this.oauthConfig = oauthConfig;
    this.oauthClient = oauthClient;
    this.methods = methods;
    this.authScopes = String.join(" ", authScopes);
    this.tokenScopes = String.join(" ", tokenScopes);
  }

  @Override
  public List<AuthMethod> methods() {
    return methods;
  }

  @Override
  public String getRedirectUrl(OauthState state) {
    return UriComponentsBuilder
        .fromUriString(AUTH_URL)
        .queryParam(OAuth2ParameterNames.RESPONSE_TYPE, "code")
        .queryParam(OAuth2ParameterNames.CLIENT_ID, oauthConfig.microsoft().clientId())
        .queryParam(OAuth2ParameterNames.REDIRECT_URI, oauthConfig.redirectUri())
        .queryParam(OAuth2ParameterNames.SCOPE, authScopes)
        .queryParam(OAuth2ParameterNames.STATE, state.encode())
        .queryParam("prompt", "select_account") // custom ms param
        .toUriString();
  }

  @Override
  public OauthResult handleAuthorizationCode(String code) {
    return oauthClient.exchangeAuthorizationCode(
        TOKEN_URL,
        oauthConfig.microsoft(),
        code,
        Map.of(OAuth2ParameterNames.SCOPE, tokenScopes));
  }

  @Override
  public boolean supportsRefresh() {
    return true;
  }

  @Override
  public OauthResult refresh(SecretString refreshToken) {
    return oauthClient.exchangeRefreshToken(
        TOKEN_URL,
        oauthConfig.microsoft(),
        refreshToken,
        Map.of(OAuth2ParameterNames.SCOPE, tokenScopes));
  }
}
