package com.UoU.infra.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.UoU._fakes.oauth.FakeOauthClient;
import com.UoU._helpers.TestData;
import com.UoU.core.auth.AuthMethod;
import com.UoU.core.auth.OauthState;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Test;

class MicrosoftCalendarOauthHandlerTests {

  @Test
  void getRedirectUrl_shouldWork() {
    val handler = new MicrosoftCalendarOauthHandler(
        TestData.oauthConfig(), mock(OauthClient.class));
    val state = new OauthState(AuthMethod.MS_OAUTH_SA, UUID.randomUUID());

    val result = handler.getRedirectUrl(state);
    assertThat(result).isNotBlank();
  }

  @Test
  void handleAuthorizationCode_shouldWork() {
    val code = TestData.uuidString();
    val oauthClientSpy = spy(new FakeOauthClient());
    val handler = new MicrosoftCalendarOauthHandler(TestData.oauthConfig(), oauthClientSpy);

    assertThatCode(() -> handler.handleAuthorizationCode(code)).doesNotThrowAnyException();
  }
}
