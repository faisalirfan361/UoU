package com.UoU._integration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import com.UoU._fakes.AuthServiceSpy;
import com.UoU._fakes.EventPublisherMock;
import com.UoU._fakes.FakeGraphServiceClient;
import com.UoU._fakes.NoopTaskScheduler;
import com.UoU._fakes.nylas.FakeInboundSyncLocker;
import com.UoU._fakes.nylas.FakeNylasAuthService;
import com.UoU._fakes.nylas.NoopNylasTaskScheduler;
import com.UoU._fakes.nylas.NylasMockFactory;
import com.UoU._fakes.oauth.FakeOauthClient;
import com.UoU._helpers.TestData;
import com.UoU._integration.core.nylas.NylasTaskRunner;
import com.UoU._integration.core.nylas.tasks.BaseNylasTaskTest;
import com.UoU.core._helpers.ValidatorWrapperFactory;
import com.UoU.core.accounts.AccountRepository;
import com.UoU.core.accounts.ServiceAccountRepository;
import com.UoU.core.auth.AuthCodeRepository;
import com.UoU.core.auth.AuthService;
import com.UoU.core.auth.OauthHandlerProvider;
import com.UoU.core.auth.serviceaccountsettings.AuthSettingsHandlerProvider;
import com.UoU.core.auth.serviceaccountsettings.GoogleJsonSettingsHandler;
import com.UoU.core.auth.serviceaccountsettings.MicrosoftOauthSettingsHandler;
import com.UoU.core.calendars.CalendarRepository;
import com.UoU.core.conferencing.ConferencingUserRepository;
import com.UoU.core.conferencing.teams.AuthHttpInterceptor;
import com.UoU.core.conferencing.teams.TeamsAuthService;
import com.UoU.core.conferencing.teams.TeamsService;
import com.UoU.core.events.EventRepository;
import com.UoU.core.nylas.ExternalEtagRepository;
import com.UoU.core.nylas.auth.NylasAuthService;
import com.UoU.core.nylas.mapping.NylasAccountMapper;
import com.UoU.core.nylas.mapping.NylasCalendarMapper;
import com.UoU.core.nylas.mapping.NylasEventMapper;
import com.UoU.core.nylas.tasks.EventHelper;
import com.UoU.core.nylas.tasks.NylasTaskScheduler;
import com.UoU.core.tasks.TaskScheduler;
import com.UoU.core.validation.ValidatorWrapper;
import com.UoU.infra.oauth.OauthClient;
import java.util.List;
import lombok.val;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

/**
 * Application configuration modifications for integration tests.
 *
 * <p>Example of how to override a bean in the DI container for testing:
 * <pre>{@code
 *   @Bean
 *   public TheBeanType theBeanName() {
 *     return new SomeFakeVersionOfTheBean();
 *   }
 * }</pre>
 */
@TestConfiguration
class Configuration {

  @Bean
  public NylasTaskScheduler nylasTaskProducer() {
    return new NoopNylasTaskScheduler();
  }

  @Bean
  public TaskScheduler taskProducer() {
    return new NoopTaskScheduler();
  }

  @Bean
  @Scope(SCOPE_PROTOTYPE)
  public BaseNylasTaskTest.TestDependencies nylasTaskTestDependencies(
      AuthCodeRepository authCodeRepo,
      ServiceAccountRepository serviceAccountRepo,
      AccountRepository accountRepo,
      CalendarRepository calendarRepo,
      EventRepository eventRepo,
      ExternalEtagRepository etagRepo,
      ConferencingUserRepository conferencingUserRepo,
      NylasAccountMapper nylasAccountMapper,
      NylasCalendarMapper nylasCalendarMapper,
      NylasEventMapper nylasEventMapper,
      OauthHandlerProvider oauthHandlerProvider) {

    val appClientMock = NylasMockFactory.createApplicationClient();
    val accountClientMock = NylasMockFactory.createAccountClientMock();
    val clientFactoryMock = NylasMockFactory
        .createClientFactoryMock(accountClientMock, appClientMock);
    val nylasAuthService = nylasAuthService();
    val authService = new AuthService(
        authCodeRepo, serviceAccountRepo, accountRepo, conferencingUserRepo,
        ValidatorWrapperFactory.createRealInstance(), oauthHandlerProvider,
        new AuthSettingsHandlerProvider(List.of(
            new MicrosoftOauthSettingsHandler(), new GoogleJsonSettingsHandler())),
        nylasAuthService, mock(NylasTaskScheduler.class));
    val eventsConfig = TestData.eventsConfig();
    val eventHelper = new EventHelper(
        clientFactoryMock, accountRepo, calendarRepo, eventsConfig);
    val nylasEventMapperSpy = spy(nylasEventMapper);
    val eventPublisherMock = new EventPublisherMock();
    val inboundSyncLocker = new FakeInboundSyncLocker();
    val internalCalendarsConfig = TestData.internalCalendarsConfig();
    val nylasTaskRunner = spy(new NylasTaskRunner(
        clientFactoryMock, accountRepo, calendarRepo, eventRepo, etagRepo, nylasAccountMapper,
        nylasCalendarMapper, nylasEventMapperSpy, authService, nylasAuthService, eventHelper,
        eventPublisherMock, inboundSyncLocker, internalCalendarsConfig));

    return new BaseNylasTaskTest.TestDependencies(
        nylasTaskRunner, appClientMock, accountClientMock, nylasEventMapperSpy, eventsConfig,
        eventPublisherMock, internalCalendarsConfig);
  }

  /**
   * Overrides AuthService with a spy, which will mostly work normally but allow mockito stuff.
   */
  @Bean
  public AuthServiceSpy authService(
      AuthCodeRepository authCodeRepo,
      ServiceAccountRepository serviceAccountRepo,
      AccountRepository accountRepo,
      ConferencingUserRepository conferencingUserRepo,
      ValidatorWrapper validator,
      OauthHandlerProvider oauthHandlerProvider,
      AuthSettingsHandlerProvider authSettingsProvider,
      NylasAuthService nylasAuthService,
      NylasTaskScheduler nylasTaskScheduler) {
    return spy(new AuthServiceSpy(
        authCodeRepo,
        serviceAccountRepo,
        accountRepo,
        conferencingUserRepo,
        validator,
        oauthHandlerProvider,
        authSettingsProvider,
        nylasAuthService,
        nylasTaskScheduler));
  }

  /**
   * Overrides Nylas auth service to return fake auth results instead of calling Nylas.
   */
  @Bean
  public NylasAuthService nylasAuthService() {
    return new FakeNylasAuthService();
  }


  /**
   * Overrides the OAuth client used by MS/Google handlers to avoid real HTTP calls.
   */
  @Bean
  public OauthClient oauthClient() {
    return new FakeOauthClient();
  }

  /**
   * Overrides the Teams Graph SDK auth interceptor to allow auth for localhost and non-https.
   */
  @Bean
  public AuthHttpInterceptor authHttpInterceptor(TeamsAuthService teamsAuthService) {
    return new AuthHttpInterceptor(teamsAuthService, List.of("localhost"), false);
  }

  /**
   * Create a fake graph service client that prevents real MS API calls.
   */
  @Bean
  public FakeGraphServiceClient fakeGraphServiceClient(AuthHttpInterceptor authHttpInterceptor) {
    return new FakeGraphServiceClient(authHttpInterceptor);
  }

  /**
   * Overrides TeamsService to use our fake graph service client.
   */
  @Bean
  public TeamsService teamsService(FakeGraphServiceClient fakeGraphServiceClient) {
    return new TeamsService(fakeGraphServiceClient);
  }
}
