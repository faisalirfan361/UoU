package com.UoU.infra.oauth;

import com.UoU.core.SecretString;
import com.UoU.core.auth.OauthConfig;
import com.UoU.core.auth.OauthException;
import com.UoU.core.auth.OauthResult;
import java.util.Map;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Client for OAuth HTTP requests, so OAuth handlers don't have to build requests manually.
 */
@Service
@AllArgsConstructor
public class OauthClient {
  private final RestTemplate restTemplate;
  private final OauthConfig oauthConfig;

  /**
   * POSTs to the tokenUrl to exchange the code for a token.
   */
  public OauthResult exchangeAuthorizationCode(
      String tokenUrl,
      OauthConfig.OauthCredentials credentials,
      String code) {
    return exchangeAuthorizationCode(tokenUrl, credentials, code, Map.of());
  }

  /**
   * POSTs to the tokenUrl to exchange the code for tokens.
   */
  public OauthResult exchangeAuthorizationCode(
      String tokenUrl,
      OauthConfig.OauthCredentials credentials,
      String code,
      Map<String, String> extraParams) {

    return fetchTokens(tokenUrl, credentials, params -> {
      params.add(
          OAuth2ParameterNames.GRANT_TYPE,
          AuthorizationGrantType.AUTHORIZATION_CODE.getValue());
      params.add(OAuth2ParameterNames.CODE, code);
      params.add(OAuth2ParameterNames.REDIRECT_URI, oauthConfig.redirectUri());
      extraParams.forEach(params::add);
    });
  }

  /**
   * POSTs to the tokenUrl to get new tokens with an existing refresh token.
   */
  public OauthResult exchangeRefreshToken(
      String tokenUrl,
      OauthConfig.OauthCredentials credentials,
      SecretString refreshToken,
      Map<String, String> extraParams) {

    return fetchTokens(tokenUrl, credentials, params -> {
      params.add(
          OAuth2ParameterNames.GRANT_TYPE,
          AuthorizationGrantType.REFRESH_TOKEN.getValue());
      params.add(OAuth2ParameterNames.REFRESH_TOKEN, refreshToken.value());
      extraParams.forEach(params::add);
    });
  }

  /**
   * POSTs to the tokenUrl to get tokens, with request customization via the paramsCustomizer.
   *
   * <p>Use paramsCustomizer to set GRANT_TYPE and any other params specific to the grant. The
   * client id and secret will be set automatically with the the passed credentials.
   */
  private OauthResult fetchTokens(
      String tokenUrl,
      OauthConfig.OauthCredentials credentials,
      Consumer<LinkedMultiValueMap<String, String>> paramsCustomizer
  ) {
    val headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    // Use a MultiValueMap for the body so RestTemplate will stringify and url-encode on send.
    val body = new LinkedMultiValueMap<String, String>();
    body.add(OAuth2ParameterNames.CLIENT_ID, credentials.clientId());
    body.add(OAuth2ParameterNames.CLIENT_SECRET, credentials.clientSecret().value());

    paramsCustomizer.accept(body);
    if (!body.containsKey(OAuth2ParameterNames.GRANT_TYPE)) {
      throw new IllegalArgumentException("GRANT_TYPE must be set via paramsCustomizer");
    }

    TokenResponse response;
    try {
      response = restTemplate.postForObject(
          tokenUrl,
          new HttpEntity<>(body, headers),
          TokenResponse.class);
    } catch (HttpStatusCodeException ex) {
      throw new OauthException(String.format(
          "OAuth token exchange got status %s from %s", ex.getStatusCode(), tokenUrl), ex);
    } catch (RestClientException ex) {
      throw new OauthException(String.format(
          "OAuth token exchange failed for %s: %s", tokenUrl, ex.getMessage()), ex);
    }

    return new OauthResult(
        response.getNameFromIdToken(),
        response.getEmailFromIdToken(),
        response.refreshToken(),
        response.accessToken(),
        response.expiresIn());
  }
}
