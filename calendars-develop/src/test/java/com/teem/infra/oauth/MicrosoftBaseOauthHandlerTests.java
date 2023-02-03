package com.UoU.infra.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.UoU._fakes.oauth.FakeOauthClient;
import com.UoU._helpers.TestData;
import com.UoU.core.auth.AuthMethod;
import com.UoU.core.auth.OauthConfig;
import com.UoU.core.auth.OauthState;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

class MicrosoftBaseOauthHandlerTests {

  @Test
  void getRedirectUrl_shouldReturnMicrosoftUrlWithAuthScopes() {
    val handler = new TestHandler(mock(OauthClient.class));
    val state = new OauthState(AuthMethod.MS_OAUTH_SA, UUID.randomUUID());

    val result = handler.getRedirectUrl(state);
    val uri = URI.create(result);

    assertThat(uri).hasAuthority("login.microsoftonline.com");
    assertThat(uri.getQuery()).contains("scope=" + String.join(" ", TestHandler.AUTH_SCOPES));
  }

  @Test
  void handleAuthorizationCode_shouldDelegateToOauthClientWithTokenScopes() {
    val code = TestData.uuidString();
    val oauthClientSpy = spy(new FakeOauthClient());
    val handler = new TestHandler(oauthClientSpy);

    handler.handleAuthorizationCode(code);

    verify(oauthClientSpy).exchangeAuthorizationCode(
        contains("microsoftonline.com"),
        any(OauthConfig.OauthCredentials.class),
        eq(code),
        argThat(x -> x
            .get(OAuth2ParameterNames.SCOPE)
            .equals(String.join(" ", TestHandler.TOKEN_SCOPES))));
  }

  private static class TestHandler extends MicrosoftBaseOauthHandler {

    public static List<AuthMethod> METHODS = List.of(AuthMethod.MS_OAUTH_SA);
    public static List<String> AUTH_SCOPES = List.of("auth-scope-1", "auth-scope-2");
    public static List<String> TOKEN_SCOPES = List.of("token-scope-1", "token-scope-1");

    protected TestHandler(OauthClient oauthClient) {
      super(TestData.oauthConfig(), oauthClient, METHODS, AUTH_SCOPES, TOKEN_SCOPES);
    }
  }
}
