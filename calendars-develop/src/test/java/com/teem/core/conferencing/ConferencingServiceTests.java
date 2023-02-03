package com.UoU.core.conferencing;

import static com.UoU.core._helpers.ValidationAssertions.assertViolationExceptionForField;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU.core.OrgId;
import com.UoU.core.accounts.Provider;
import com.UoU.core.auth.AuthMethod;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.calendars.CalendarRepository;
import com.UoU.core.conferencing.teams.TeamsService;
import com.UoU.core.events.EventCreateRequest;
import com.UoU.core.exceptions.NotFoundException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.val;
import org.junit.jupiter.api.Test;

class ConferencingServiceTests {

  @Test
  void addConferencingToEvent_shouldReturnOriginalRequestWhenNullOrNoConferencing() {
    val scenario = new Scenario();
    val nullRequest = (EventCreateRequest) null;
    val nullConferencingRequest = scenario.buildEventCreateRequest()
        .conferencing(null)
        .build();

    val nullResult = scenario.service.addConferencingToEvent(nullRequest);
    val nullConferencingResult = scenario.service.addConferencingToEvent(nullConferencingRequest);

    assertThat(nullResult).isSameAs(nullRequest);
    assertThat(nullConferencingResult).isSameAs(nullConferencingRequest);
  }

  @Test
  @SuppressWarnings("unchecked")
  void addConferencingToEvent_shouldDelegateToTeamsServiceForTeamsAuthMethod() {
    val scenario = new Scenario();
    val request = scenario
        .buildEventCreateRequest()
        .conferencing(new ConferencingMeetingCreateRequest(
            scenario.userEmail, scenario.userId, "en-US"))
        .build();

    scenario.service.addConferencingToEvent(request);

    verify(scenario.deps.teamsServiceMock).addConferencingToEvent(
        eq(request),
        eq(scenario.userId),
        any(Supplier.class),
        any(Provider.class),
        eq(Locale.US));
  }

  @Test
  void addConferencingToEvent_shouldThrowForUserNotFound() {
    val scenario = new Scenario();
    val request = scenario
        .buildEventCreateRequest()
        .conferencing(new ConferencingMeetingCreateRequest(
            scenario.userEmail, ConferencingUserId.create(), null))
        .build();

    assertViolationExceptionForField(
        () -> scenario.service.addConferencingToEvent(request),
        "conferencing.autoCreate.userId",
        "not found");
  }

  @Test
  void addConferencingToEvent_shouldThrowForUserInDifferentOrg() {
    val scenario = new Scenario();
    val request = scenario
        .buildEventCreateRequest()
        .orgId(TestData.orgId()) // different org
        .build();

    assertViolationExceptionForField(
        () -> scenario.service.addConferencingToEvent(request),
        "conferencing.autoCreate.userId",
        "not found");
  }

  @Test
  void addConferencingToEvent_shouldThrowForUserEmailDifferentFromPrincipalEmail() {
    val scenario = new Scenario();
    val request = scenario
        .buildEventCreateRequest()
        .conferencing(new ConferencingMeetingCreateRequest(
            TestData.email(), scenario.userId, null))
        .build();

    assertViolationExceptionForField(
        () -> scenario.service.addConferencingToEvent(request),
        "conferencing.autoCreate.userId",
        "email is invalid");
  }

  private static class Scenario {
    private final OrgId orgId = TestData.orgId();
    private final CalendarId calendarId = CalendarId.create();
    private final ConferencingUserId userId = ConferencingUserId.create();
    private final String userEmail = TestData.email();
    private final Dependencies deps = new Dependencies(
        mock(CalendarRepository.class),
        mock(ConferencingUserRepository.class),
        mock(TeamsService.class));
    private final ConferencingService service = new ConferencingService(
        deps.calendarRepoMock,
        deps.conferencingUserRepoMock,
        deps.teamsServiceMock);

    public Scenario() {
      when(deps.calendarRepoMock.getAccountProvider(any())).then(inv -> Optional
          .ofNullable((CalendarId) inv.getArgument(0))
          .filter(id -> id.equals(calendarId))
          .map(id -> Provider.GOOGLE));

      when(deps.conferencingUserRepoMock.get(any())).then(inv -> Optional
          .ofNullable((ConferencingUserId) inv.getArgument(0))
          .filter(id -> id.equals(userId))
          .map(id -> ModelBuilders.conferencingUser()
              .id(userId)
              .orgId(orgId)
              .name("test")
              .email(userEmail)
              .authMethod(AuthMethod.CONF_TEAMS_OAUTH)
              .createdAt(Instant.now())
              .expireAt(Instant.now().plus(20, ChronoUnit.MINUTES))
              .build())
          .orElseThrow(() -> NotFoundException.ofClass(ConferencingUser.class)));
    }

    public EventCreateRequest.Builder buildEventCreateRequest() {
      return ModelBuilders.eventCreateRequestWithTestData()
          .orgId(orgId)
          .calendarId(calendarId)
          .conferencing(new ConferencingMeetingCreateRequest(userEmail, userId, null));
    }

    public ModelBuilders.ConferencingUserBuilder buildUser() {
      return ModelBuilders.conferencingUserWithTestData()
          .id(userId)
          .authMethod(AuthMethod.CONF_TEAMS_OAUTH)
          .orgId(orgId);
    }

    private record Dependencies(
        CalendarRepository calendarRepoMock,
        ConferencingUserRepository conferencingUserRepoMock,
        TeamsService teamsServiceMock
    ) {
    }
  }
}
