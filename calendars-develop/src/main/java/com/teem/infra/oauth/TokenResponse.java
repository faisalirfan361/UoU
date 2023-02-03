package com.UoU.infra.oauth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nimbusds.jwt.JWTParser;
import com.UoU.core.SecretString;
import com.UoU.core.auth.OauthException;
import java.util.Optional;

/**
 * Represents a token response from an OAuth provider (Microsoft, Google).
 */
record TokenResponse(
    SecretString idToken,
    SecretString refreshToken,
    SecretString accessToken,
    Integer expiresIn
) {

  private static final String CLAIM_EMAIL = "email";
  private static final String CLAIM_NAME = "name";

  @JsonCreator
  public TokenResponse(
      @JsonProperty("id_token") String idToken,
      @JsonProperty("refresh_token") String refreshToken,
      @JsonProperty("access_token") String accessToken,
      @JsonProperty("expires_in") Integer expiresIn) {
    this(new SecretString(idToken),
        new SecretString(refreshToken),
        new SecretString(accessToken),
        expiresIn);
  }

  public TokenResponse {
    if (idToken == null || idToken.isBlank()
        || refreshToken == null || refreshToken.isBlank()
        || accessToken == null || accessToken.isBlank()) {
      throw new OauthException("Missing id token, refresh token, or access token in response");
    }
  }

  public String getEmailFromIdToken() {
    try {
      return Optional.of(JWTParser
              .parse(idToken.value())
              .getJWTClaimsSet()
              .getStringClaim(CLAIM_EMAIL))
          .filter(x -> !x.isBlank())
          .orElseThrow();
    } catch (Exception ex) {
      throw new OauthException("Email could not be parsed from token response", ex);
    }
  }

  public String getNameFromIdToken() {
    try {
      return Optional.of(JWTParser
              .parse(idToken.value())
              .getJWTClaimsSet()
              .getStringClaim(CLAIM_NAME))
          .filter(x -> !x.isBlank())
          .orElseThrow();
    } catch (Exception ex) {
      throw new OauthException("Name could not be parsed from token response", ex);
    }
  }
}
