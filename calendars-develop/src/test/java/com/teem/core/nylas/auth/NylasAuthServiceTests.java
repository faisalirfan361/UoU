package com.UoU.core.nylas.auth;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThatCode;
import static org.mockito.Mockito.when;

import com.nylas.RequestFailedException;
import com.UoU._fakes.nylas.NylasMockFactory;
import com.UoU._helpers.TestData;
import com.UoU.core.SecretString;
import com.UoU.core.accounts.ServiceAccountAuthInfo;
import com.UoU.core.auth.AuthMethod;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;
import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.Test;

class NylasAuthServiceTests {

  @Test
  void auth_shouldThrowNylasAuthExceptionForNylas400() {
    val message = "message-" + TestData.uuidString();
    val exception = new RequestFailedException(400, message, "some_type");

    assertThatAuthNylasException(exception, x -> x
        .isInstanceOf(NylasAuthException.class)
        .hasMessageContaining(message)
        .hasCause(exception));
  }

  @Test
  void auth_shouldThrowNylasAuthExceptionForNylas401() {
    val message = "message-" + TestData.uuidString();
    val exception = new RequestFailedException(401, message, "some_type");

    assertThatAuthNylasException(exception, x -> x
        .isInstanceOf(NylasAuthException.class)
        .hasMessageContaining(message)
        .hasCause(exception));
  }

  @Test
  void auth_shouldThrowNylasAuthExceptionForNylas403() {
    val message = "message-" + TestData.uuidString();
    val exception = new RequestFailedException(403, message, "some_type");

    assertThatAuthNylasException(exception, x -> x
        .isInstanceOf(NylasAuthException.class)
        .hasMessageContaining(message)
        .hasCause(exception));
  }

  @Test
  void auth_shouldThrowNylasAuthExceptionForNylasAuthError() {
    val errorType = "auth_error";
    val message = "message-" + TestData.uuidString();
    val exception = new RequestFailedException(500, message, errorType);

    assertThatAuthNylasException(exception, x -> x
        .isInstanceOf(NylasAuthException.class)
        .hasMessageContaining(message)
        .hasCause(exception));
  }

  @Test
  void auth_shouldThrowOriginalExceptionForOtherNylasErrors() {
    val genericException = new IllegalArgumentException("oops");
    val requestFailedException = new RequestFailedException(567, "oops", "some_type");

    assertThatAuthNylasException(genericException, x -> x.isSameAs(genericException));
    assertThatAuthNylasException(requestFailedException, x -> x.isSameAs(requestFailedException));
  }

  /**
   * Calls all the auth methods and runs asserter on the thrown exception.
   */
  private void assertThatAuthNylasException(
      Exception ex, Consumer<AbstractThrowableAssert<?, ? extends Throwable>> asserter) {

    val serviceAccountAuthInfo = new ServiceAccountAuthInfo(
        TestData.serviceAccountId(),
        TestData.orgId(),
        AuthMethod.MS_OAUTH_SA,
        Map.of(
            "microsoft_client_id", "test",
            "microsoft_client_secret", "test",
            "microsoft_refresh_token", "test",
            "redirect_uri", "test",
            "service_account", "test"
        ));

    val nylasAccountClientMock = NylasMockFactory.createAccountClientMock();
    val nylasAppClientMock = NylasMockFactory.createApplicationClient();
    val nylasClientFactoryMock = NylasMockFactory.createClientFactoryMock(
        nylasAccountClientMock, nylasAppClientMock);

    // Rather than mock the whole nylas call chain, just throw when first accessing the auth client.
    when(nylasAppClientMock.nativeAuthentication())
        .then(x -> {
          throw ex;
        });

    val service = new NylasAuthService(TestData.oauthConfig(), nylasClientFactoryMock);

    asserter.accept(assertThatCode(() ->
        service.authAccount(AuthMethod.GOOGLE_OAUTH, new SecretString("token"), "name", "email")));
    asserter.accept(assertThatCode(() ->
        service.authSubaccount(serviceAccountAuthInfo, "name", "email")));
    asserter.accept(assertThatCode(() ->
        service.authVirtualAccount("name", "email", "America/Denver")));
  }
}
