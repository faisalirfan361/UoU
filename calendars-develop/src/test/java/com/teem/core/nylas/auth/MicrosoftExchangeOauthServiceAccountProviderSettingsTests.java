package com.UoU.core.nylas.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.UoU.core.nylas.auth.MicrosoftExchangeOauthServiceAccountProviderSettings;
import lombok.val;
import org.junit.jupiter.api.Test;

public class MicrosoftExchangeOauthServiceAccountProviderSettingsTests {

  @Test
  void validate_shouldValidate() {
    val settings = buildValidSettings();
    assertThatCode(() -> settings.validate()).doesNotThrowAnyException();
  }

  @Test
  void validate_shouldThrowForMissingKey() {
    val settings = buildValidSettings();
    settings.getValidatedSettings().remove("service_account");

    val ex = assertThrows(IllegalStateException.class, () -> settings.validate());
    assertThat(ex.getMessage()).contains("Service Account");
  }

  private MicrosoftExchangeOauthServiceAccountProviderSettings buildValidSettings() {
    return new MicrosoftExchangeOauthServiceAccountProviderSettings()
      .microsoftClientId("clientId")
      .microsoftClientSecret("clientSecret")
      .microsoftRefreshToken("refreshToken")
      .redirectUri("https://test.com");
  }
}
