package com.UoU.infra.oauth;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Test;

class GoogleOauthHandlerTests {

  @Test
  void getRedirectUrl_shouldWork() {
    val handler = new GoogleOauthHandler(TestData.oauthConfig(), mock(OauthClient.class));
    val state = new OauthState(AuthMethod.GOOGLE_OAUTH, UUID.randomUUID());

    val result = handler.getRedirectUrl(state);

    assertThat(URI.create(result)).isNotNull();
  }

  @Test
  void handleAuthorizationCode_shouldDelegateToOauthClient() {
    val code = TestData.uuidString();
    val oauthClientSpy = spy(new FakeOauthClient());
    val handler = new GoogleOauthHandler(TestData.oauthConfig(), oauthClientSpy);

    handler.handleAuthorizationCode(code);

    verify(oauthClientSpy).exchangeAuthorizationCode(
        contains("oauth2.googleapis.com"),
        any(OauthConfig.OauthCredentials.class),
        eq(code));
  }
}
