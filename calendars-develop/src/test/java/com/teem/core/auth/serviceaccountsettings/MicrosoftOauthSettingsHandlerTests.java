package com.UoU.core.auth.serviceaccountsettings;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.UoU._helpers.TestData;
import com.UoU.core.auth.AuthInput;
import com.UoU.core.auth.OauthResult;
import lombok.val;
import org.junit.jupiter.api.Test;

class MicrosoftOauthSettingsHandlerTests {

  @Test
  void createSettings_getRefreshTokenFromSettings_shouldRoundTrip() {
    val handler = new MicrosoftOauthSettingsHandler();
    val refreshToken = TestData.secretString();
    val oathResult = new OauthResult(
        "test", "test@example.com", refreshToken, TestData.secretString(), null);
    val authInput  = AuthInput.ofOauthResult(oathResult);

    val settings = handler.createSettings(authInput);
    val resultRefreshToken = handler.getRefreshTokenFromSettings(
        settings.settings());

    assertThat(resultRefreshToken).hasValue(refreshToken);
  }
}
