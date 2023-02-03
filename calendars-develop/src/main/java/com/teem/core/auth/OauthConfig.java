package com.UoU.core.auth;

import com.UoU.core.SecretString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configures OAuth details needed to authorize accounts with external providers.
 */
@ConfigurationProperties("oauth")
public record OauthConfig(
    String redirectUri,
    OauthCredentials microsoft,
    OauthCredentials google
) {

  public OauthConfig {
    if (redirectUri == null || redirectUri.isBlank() || microsoft == null || google == null) {
      throw new IllegalArgumentException("Invalid oauth config");
    }
  }

  /**
   * A set of OAuth credentials for an external provider.
   */
  public record OauthCredentials(
      String clientId,
      SecretString clientSecret
  ) {
    public OauthCredentials {
      if (clientId == null || clientId.isBlank()
          || clientSecret == null || clientSecret.isBlank()) {
        throw new IllegalArgumentException("Invalid oauth config credentials");
      }
    }
  }
}
