package com.UoU.core.nylas.auth;

import com.nylas.ProviderSettings;

class MicrosoftExchangeOauthServiceAccountProviderSettings extends ProviderSettings {
  public MicrosoftExchangeOauthServiceAccountProviderSettings() {
    super("exchange");
    this.add("service_account", true);
  }

  public MicrosoftExchangeOauthServiceAccountProviderSettings microsoftClientId(
      String microsoftClientId) {
    add("microsoft_client_id", microsoftClientId);
    return this;
  }

  public MicrosoftExchangeOauthServiceAccountProviderSettings microsoftClientSecret(
      String microsoftClientSecret) {
    add("microsoft_client_secret", microsoftClientSecret);
    return this;
  }

  public MicrosoftExchangeOauthServiceAccountProviderSettings microsoftRefreshToken(
      String microsoftRefreshToken) {
    add("microsoft_refresh_token", microsoftRefreshToken);
    return this;
  }

  public MicrosoftExchangeOauthServiceAccountProviderSettings redirectUri(String redirectUri) {
    add("redirect_uri", redirectUri);
    return this;
  }

  @Override
  protected void validate() {
    assertSetting("microsoft_client_id", "Microsoft Client ID is required");
    assertSetting("microsoft_client_secret", "Microsoft Client Secret is required");
    assertSetting("microsoft_refresh_token", "Microsoft Refresh Token is required");
    assertSetting("redirect_uri", "Redirect URI is required");
    assertSetting("service_account", "Service Account is required");
  }
}
