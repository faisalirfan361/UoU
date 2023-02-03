package com.UoU.core.auth.serviceaccountsettings;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.UoU.core.auth.AuthMethod;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

public class AuthSettingsHandlerProviderTests {

  @Test
  void ctor_shouldThrowForDuplicateAuthMethods() {
    val authSettings = List.of(
        createMockSettings(AuthMethod.MS_OAUTH_SA),
        createMockSettings(AuthMethod.MS_OAUTH_SA)
    );

    assertThatCode(() -> new AuthSettingsHandlerProvider(authSettings))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(AuthMethod.MS_OAUTH_SA.getValue());
  }

  @Test
  void provide_shouldThrowWhenNoneAvailable() {
    val provider = new AuthSettingsHandlerProvider(List.of());

    assertThatCode(() -> provider.provide(AuthMethod.MS_OAUTH_SA))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(AuthMethod.MS_OAUTH_SA.getValue());
  }

  private static AuthSettingsHandler createMockSettings(AuthMethod authMethod) {
    val authSettings = mock(AuthSettingsHandler.class);
    when(authSettings.authMethod()).thenReturn(authMethod);
    return authSettings;
  }
}
