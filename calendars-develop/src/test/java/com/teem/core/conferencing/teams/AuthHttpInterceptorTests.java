package com.UoU.core.conferencing.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microsoft.graph.httpcore.HttpClients;
import com.UoU._helpers.TestData;
import com.UoU.core.Noop;
import com.UoU.core.conferencing.ConferencingAuthInfo;
import com.UoU.core.conferencing.ConferencingUser;
import com.UoU.core.conferencing.ConferencingUserId;
import com.UoU.core.exceptions.NotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthHttpInterceptorTests {

  private static MockWebServer SERVER;

  private TeamsAuthService authServiceMock;
  private AuthHttpInterceptor interceptor;
  private ConferencingUserId userId;
  private HttpHeaderManager httpHeaderManager;
  private Pair<String, String> requireAuthHeader;

  @BeforeAll
  static void beforeAll() throws IOException {
    SERVER = new MockWebServer();
    SERVER.start();
  }

  @AfterAll
  static void afterAll() throws IOException {
    SERVER.shutdown();
  }

  @BeforeEach
  void beforeEach() {
    userId = ConferencingUserId.create();
    authServiceMock = mock(TeamsAuthService.class);
    interceptor = new AuthHttpInterceptor(authServiceMock, List.of(SERVER.url("").host()), false);
    httpHeaderManager = new HttpHeaderManager();
    requireAuthHeader = httpHeaderManager.createRequireAuthHeader(userId);
  }

  @Test
  void shouldAddAuthorizationHeaderWithAccessToken() {
    SERVER.enqueue(new MockResponse().setResponseCode(200));

    val authInfo = TestData.conferencingAuthInfo();
    when(authServiceMock.getAuthInfo(userId)).thenReturn(authInfo);

    val response = doRequest(interceptor, request -> request
        .addHeader(requireAuthHeader.getKey(), requireAuthHeader.getValue()));

    assertThat(response.isSuccessful()).isTrue();
    assertLastRequestWasAuthorized(authInfo);
  }

  @Test
  void shouldRefreshAuthInfoWhenExpired() {
    SERVER.enqueue(new MockResponse().setResponseCode(200));

    val expiredAuthInfo = new ConferencingAuthInfo(
        "expired", TestData.secretString(), TestData.secretString(), Instant.now());
    val freshAuthInfo = TestData.conferencingAuthInfo();

    when(authServiceMock.getAuthInfo(userId))
        .thenReturn(expiredAuthInfo);
    when(authServiceMock.refreshAndSaveAuthInfo(userId, expiredAuthInfo))
        .thenReturn(freshAuthInfo);

    val response = doRequest(interceptor, request -> request
        .addHeader(requireAuthHeader.getKey(), requireAuthHeader.getValue()));

    assertThat(response.isSuccessful()).isTrue();
    assertLastRequestWasAuthorized(freshAuthInfo);
    verify(authServiceMock).refreshAndSaveAuthInfo(userId, expiredAuthInfo);
  }

  @Test
  void shouldRefreshAuthInfoAndRetryRequestAfter401() throws InterruptedException {
    SERVER.enqueue(new MockResponse().setResponseCode(401)); // 401 to trigger refresh & retry
    SERVER.enqueue(new MockResponse().setResponseCode(200)); // 2nd call should work

    val authInfo = TestData.conferencingAuthInfo();
    val newAuthInfo = TestData.conferencingAuthInfo();

    when(authServiceMock.getAuthInfo(userId)).thenReturn(authInfo);
    when(authServiceMock.refreshAndSaveAuthInfo(userId, authInfo)).thenReturn(newAuthInfo);

    val response = doRequest(interceptor, request -> request
        .addHeader(requireAuthHeader.getKey(), requireAuthHeader.getValue()));

    val recordedRequest1 = SERVER.takeRequest(0, TimeUnit.SECONDS);
    val recordedRequest2 = SERVER.takeRequest(0, TimeUnit.SECONDS);

    assertThat(response.isSuccessful()).isTrue();
    verify(authServiceMock).refreshAndSaveAuthInfo(userId, authInfo);
    assertThat(recordedRequest1.getHeader("Authorization"))
        .isEqualTo("Bearer " + authInfo.accessToken().value());
    assertThat(recordedRequest2.getHeader("Authorization"))
        .isEqualTo("Bearer " + newAuthInfo.accessToken().value());
  }

  @Test
  void shouldNotAuthorizeWhenNoHeaderWithUserId() {
    SERVER.enqueue(new MockResponse().setResponseCode(401));

    val response = doRequest(interceptor, request -> Noop.because("no userId header"));

    assertThat(response.code()).isEqualTo(401);
    assertLastRequestWasNotAuthorized();
  }

  @Test
  void shouldNotAuthorizeForInvalidHost() {
    SERVER.enqueue(new MockResponse().setResponseCode(500));

    interceptor = new AuthHttpInterceptor(authServiceMock, List.of("other-host"), false);

    val response = doRequest(interceptor, request -> request
        .addHeader(requireAuthHeader.getKey(), requireAuthHeader.getValue()));

    assertThat(response.code()).isEqualTo(500);
    assertLastRequestWasNotAuthorized();
  }

  @Test
  void shouldPropagateUserNotFoundException() {
    val exception = NotFoundException.ofClass(ConferencingUser.class);
    when(authServiceMock.getAuthInfo(userId)).thenThrow(exception);

    // For now, just make sure repo exceptions are allowed to propagate. Depending on how
    // this gets handled later, we could introduce more specific auth exceptions.
    assertThatCode(() -> doRequest(interceptor, request -> request
        .addHeader(requireAuthHeader.getKey(), requireAuthHeader.getValue())))
        .isSameAs(exception);
  }

  @SneakyThrows
  private Response doRequest(AuthHttpInterceptor interceptor, Consumer<Request.Builder> builder) {
    val http = HttpClients.custom().addInterceptor(interceptor).build();
    val request = new Request.Builder().url(SERVER.url("")).get();
    builder.accept(request);

    return http.newCall(request.build()).execute();
  }

  @SneakyThrows
  private void assertLastRequestWasAuthorized(ConferencingAuthInfo authInfo) {
    assertThat(SERVER.takeRequest(0, TimeUnit.SECONDS).getHeader("Authorization"))
        .isEqualTo("Bearer " + authInfo.accessToken().value());
  }

  @SneakyThrows
  private void assertLastRequestWasNotAuthorized() {
    assertThat(SERVER.takeRequest(0, TimeUnit.SECONDS).getHeader("Authorization")).isNull();
  }
}
