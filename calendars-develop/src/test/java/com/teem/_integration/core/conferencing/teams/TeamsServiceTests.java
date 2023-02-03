package com.UoU._integration.core.conferencing.teams;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.graph.models.OnlineMeeting;
import com.UoU._fakes.FakeGraphServiceClient;
import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU._integration.BaseAppIntegrationTest;
import com.UoU.core.accounts.Provider;
import com.UoU.core.auth.AuthMethod;
import com.UoU.core.conferencing.teams.TeamsService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests the entire TeamsService stack, except external http calls to Microsoft are faked.
 *
 * <p>More detailed tests can be done elsewhere for individual components, but we want a few basic
 * tests here to ensure all the components work together.
 */
public class TeamsServiceTests extends BaseAppIntegrationTest {
  private static MockWebServer SERVER;

  @Autowired
  private FakeGraphServiceClient fakeGraphServiceClient;
  @Autowired private ObjectMapper objectMapper;
  private OnlineMeeting fakeMeeting;
  private TeamsService service;

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
  @SneakyThrows
  void beforeEach() {
    fakeGraphServiceClient.setServiceRoot(SERVER.url("/test").toString());
    service = new TeamsService(fakeGraphServiceClient);

    fakeMeeting = TestData.teamsMeeting();
    SERVER.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(objectMapper.writeValueAsString(fakeMeeting)));
  }

  @Test
  @SneakyThrows
  void addConferencingToEvent_shouldAddJoinInfoToEventDescription() {
    val userId = dbHelper.createConferencingUser(orgId, AuthMethod.CONF_TEAMS_OAUTH);

    val request = ModelBuilders
        .eventCreateRequestWithTestData()
        .title("Title " + TestData.uuidString())
        .description("Description " + TestData.uuidString())
        .build();

    val result = service.addConferencingToEvent(
        request, userId, () -> ZoneOffset.UTC, Provider.MICROSOFT, Locale.US);
    val recordedRequestBody = SERVER.takeRequest(0, TimeUnit.SECONDS)
        .getBody()
        .readString(StandardCharsets.UTF_8);

    assertThat(result.description()).contains(request.description());
    assertThat(result.description()).contains("Microsoft Teams meeting");
    assertThat(recordedRequestBody).contains(request.id().value().toString());
    assertThat(recordedRequestBody).contains(request.title());
  }

  @Test
  @SneakyThrows
  void createOrGetMeeting_shouldWorkAndAuthorizeUser() {
    val userId = dbHelper.createConferencingUser(orgId, AuthMethod.CONF_TEAMS_OAUTH);

    val response = service.createOrGetMeeting(userId, Locale.US, req -> req
        .withSubject(fakeMeeting.subject)
        .withExternalId("test")
        .build());
    val recordedRequest = SERVER.takeRequest(0, TimeUnit.SECONDS);

    assertThat(response.id).isEqualTo(fakeMeeting.id);
    assertThat(response.subject).isEqualTo(fakeMeeting.subject);
    assertThat(recordedRequest.getHeader("Authorization")).startsWith("Bearer");
  }

  @Test
  @SneakyThrows
  void createOrGetMeeting_shouldRefreshExpiredUserAuthorization() {
    val expiredAccessToken = TestData.secretString();
    val userId = dbHelper.createConferencingUser(orgId, x -> x
        .accessToken(expiredAccessToken)
        .expireAt(Instant.now())); // mark as expired

    val response = service.createOrGetMeeting(userId, Locale.UK, req -> req.build());
    val recordedRequest = SERVER.takeRequest(0, TimeUnit.SECONDS);
    val resultAccessToken = dbHelper.getConferencingUserRepo().getAuthInfo(userId).accessToken();

    assertThat(response.id).isEqualTo(fakeMeeting.id);
    assertThat(response.subject).isEqualTo(fakeMeeting.subject);
    assertThat(resultAccessToken).isNotEqualTo(expiredAccessToken);
    assertThat(recordedRequest.getHeader("Authorization"))
        .isEqualTo("Bearer " + resultAccessToken.value());
  }

  @ParameterizedTest
  @EnumSource(AcceptLanguageScenario.class)
  @SneakyThrows
  void createOrGetMeeting_shouldAddAcceptLanguageHeader(AcceptLanguageScenario scenario) {
    val userId = dbHelper.createConferencingUser(orgId, AuthMethod.CONF_TEAMS_OAUTH);

    service.createOrGetMeeting(userId, scenario.locale, req -> req.build());
    val recordedRequest = SERVER.takeRequest(0, TimeUnit.SECONDS);

    assertThat(recordedRequest.getHeader("Accept-Language")).isEqualTo(scenario.expectedHeader);
  }

  @AllArgsConstructor
  private enum AcceptLanguageScenario {
    NULL(null, null),
    INVALID(Locale.forLanguageTag("invalid"), "invalid,en"),
    ENGLISH(Locale.ENGLISH, "en"),
    US(Locale.US, "en-US,en"),
    PT_BR(Locale.forLanguageTag("pt-BR"), "pt-BR,pt,en");

    public final Locale locale;
    public final String expectedHeader;
  }
}
