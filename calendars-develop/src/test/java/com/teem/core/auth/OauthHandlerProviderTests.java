package com.UoU.core.auth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class OauthHandlerProviderTests {

  @Test
  void ctor_shouldThrowForMultipleHandlersPerAuthMethod() {
    val handlers = List.of(
        createMockHandler(List.of(AuthMethod.GOOGLE_OAUTH)),
        createMockHandler(List.of(AuthMethod.GOOGLE_SA, AuthMethod.GOOGLE_OAUTH)));

    assertThatCode(() -> new OauthHandlerProvider(handlers))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(AuthMethod.GOOGLE_OAUTH.getValue());
  }

  @Test
  void provide_shouldProvideBasedOnHandlerMethods() {
    val googleHandler = createMockHandler(List.of(AuthMethod.GOOGLE_SA, AuthMethod.GOOGLE_OAUTH));
    val msHandler = createMockHandler(List.of(AuthMethod.MS_OAUTH_SA));
    val handlers = List.of(msHandler, googleHandler);
    val provider = new OauthHandlerProvider(handlers);

    val result = provider.provide(AuthMethod.GOOGLE_OAUTH);

    assertThat(result).isEqualTo(googleHandler);
  }

  @Test
  void provide_shouldThrowWhenNoHandlerConfigured() {
    val provider = new OauthHandlerProvider(List.of());

    assertThatCode(() -> provider.provide(AuthMethod.MS_OAUTH_SA))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("handler");
  }

  private static OauthHandler createMockHandler(List<AuthMethod> methods) {
    val handler = mock(OauthHandler.class);
    when(handler.methods()).thenReturn(methods);
    return handler;
  }
}
