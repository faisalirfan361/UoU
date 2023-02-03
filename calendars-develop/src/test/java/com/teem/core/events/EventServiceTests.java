package com.UoU.core.events;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertViolationExceptionForField;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.UoU._fakes.EventPublisherMock;
import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU.core.OrgId;
import com.UoU.core._helpers.ValidatorWrapperFactory;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.calendars.Calendar;
import com.UoU.core.calendars.CalendarAccessInfo;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.calendars.CalendarRepository;
import com.UoU.core.conferencing.ConferencingMeetingCreateRequest;
import com.UoU.core.conferencing.ConferencingService;
import com.UoU.core.conferencing.ConferencingUserId;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.exceptions.ReadOnlyException;
import com.UoU.core.nylas.tasks.NylasTaskScheduler;
import com.UoU.core.validation.ValidatorWrapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.groovy.util.Arrays;
import org.junit.jupiter.api.Test;

class EventServiceTests {

  @Test
  void list_shouldValidateAndFail() {
    val scenario = new Scenario();
    val request = EventQuery.builder().build();

    assertThatValidationFails(() -> scenario.service.list(request));
  }

  @Test
  void create_shouldSyncWhenAccountIdExists() {
    val scenario = new Scenario()
        .withAccount()
        .withCalendarAccess(x -> new CalendarAccessInfo(x.orgId, false));
    val request = scenario.buildEventCreateRequest().build();

    assertThatCode(() -> scenario.service.create(request))
        .doesNotThrowAnyException();

    verify(scenario.deps.eventRepoMock).create(request);
    verify(scenario.deps.nylasTaskSchedulerMock)
        .exportEventToNylas(scenario.accountId.orElseThrow(), request.id());
  }

  @Test
  void create_shouldNotSyncWhenNoAccountId() {
    val scenario = new Scenario()
        .withCalendarAccess(x -> new CalendarAccessInfo(x.orgId, false));
    val request = scenario.buildEventCreateRequest().build();

    assertThatCode(() -> scenario.service.create(request))
        .doesNotThrowAnyException();

    verify(scenario.deps.eventRepoMock).create(request);
    verifyNoInteractions(scenario.deps.nylasTaskSchedulerMock);
  }

  @Test
  void create_shouldPublishEventChanged() {
    val scenario = new Scenario()
        .withCalendarAccess(x -> new CalendarAccessInfo(x.orgId, false));
    val request = scenario.buildEventCreateRequest().build();

    scenario.service.create(request);

    scenario.deps.eventPublisherMock.verify()
        .hasEventCreated(scenario.eventId)
        .noEventUpdated()
        .noEventDeleted();
  }

  @Test
  void create_shouldCallConferencingServiceWhenConferencingPropertyExists() {
    val scenario = new Scenario()
        .withCalendarAccess(x -> new CalendarAccessInfo(x.orgId, false));

    val request = scenario.buildEventCreateRequest().build();
    val requestWithConferencing = scenario.buildEventCreateRequest()
        .conferencing(new ConferencingMeetingCreateRequest(
            TestData.email(), ConferencingUserId.create(), "en"))
        .build();

    scenario.service.create(request);
    verifyNoInteractions(scenario.deps.conferencingServiceMock);

    scenario.service.create(requestWithConferencing);
    verify(scenario.deps.conferencingServiceMock).addConferencingToEvent(requestWithConferencing);
  }

  @Test
  void create_shouldThrowForCalendarInDifferentOrg() {
    val scenario = new Scenario()
        .withAccount()
        .withCalendarAccess(x -> new CalendarAccessInfo(x.orgId, false));
    val request = scenario.buildEventCreateRequest()
        .orgId(new OrgId("different org"))
        .build();

    assertViolationExceptionForField(
        () -> scenario.service.create(request), "calendarId", "not found");

    verifyNoInteractions(scenario.deps.eventRepoMock);
    verifyNoInteractions(scenario.deps.nylasTaskSchedulerMock);
  }

  @Test
  void create_shouldThrowForReadOnlyCalendar() {
    val scenario = new Scenario()
        .withAccount()
        .withCalendarAccess(x -> new CalendarAccessInfo(x.orgId, true));
    val request = scenario.buildEventCreateRequest().build();

    assertViolationExceptionForField(
        () -> scenario.service.create(request), "calendarId", "not found");

    verifyNoInteractions(scenario.deps.eventRepoMock);
    verifyNoInteractions(scenario.deps.nylasTaskSchedulerMock);
  }

  @Test
  void create_shouldValidateAndFail() {
    val scenario = new Scenario();
    val request = scenario.buildEventCreateRequest()
        .calendarId(null) // invalid
        .build();

    assertThatValidationFails(() -> scenario.service.create(request));
  }

  @Test
  void create_shouldValidateEventsActivePeriod() {
    val scenario = new Scenario();
    val activePeriodStart = scenario.eventsConfig.activePeriod().current().startAtUtcOffset();
    val request = scenario.buildEventCreateRequest()
        .when(new When.Date(activePeriodStart.toLocalDate().minusDays(1)))
        .build();

    assertViolationExceptionForField(
        () -> scenario.service.create(request),
        "when.date",
        "active period");
  }

  @Test
  void create_shouldValidateRecurrenceMasterStartIsWholeMinutes() {
    val scenario = new Scenario()
        .withCalendarAccess(x -> new CalendarAccessInfo(x.orgId, false));
    val validStart = Instant.now().truncatedTo(ChronoUnit.MINUTES);
    val validRequest = scenario.buildEventCreateRequest()
        .recurrence(TestData.recurrenceMaster())
        .when(ModelBuilders.whenTimeSpan()
            .startTime(validStart)
            .endTime(validStart.plusSeconds(300))
            .build())
        .build();
    val invalidRequest = scenario.buildEventCreateRequest()
        .recurrence(TestData.recurrenceMaster())
        .when(ModelBuilders.whenTimeSpan()
            .startTime(validStart.plusSeconds(30))
            .endTime(validStart.plusSeconds(300))
            .build())
        .build();

    assertThatCode(() -> scenario.service.create(validRequest))
        .doesNotThrowAnyException();
    assertViolationExceptionForField(
        () -> scenario.service.create(invalidRequest),
        "when.startTime",
        "start");
  }

  @Test
  void create_shouldValidateRecurrenceInstanceStartIsWholeMinutes() {
    val scenario = new Scenario()
        .withCalendarAccess(x -> new CalendarAccessInfo(x.orgId, false));
    val validStart = Instant.now().truncatedTo(ChronoUnit.MINUTES);
    val validRequest = scenario.buildEventCreateRequest()
        .recurrence(TestData.recurrenceInstance())
        .when(ModelBuilders.whenTimeSpan()
            .startTime(validStart)
            .endTime(validStart.plusSeconds(300))
            .build())
        .build();
    val invalidRequest = scenario.buildEventCreateRequest()
        .recurrence(TestData.recurrenceInstance())
        .when(ModelBuilders.whenTimeSpan()
            .startTime(validStart.plusSeconds(30))
            .endTime(validStart.plusSeconds(300))
            .build())
        .build();

    assertThatCode(() -> scenario.service.create(validRequest))
        .doesNotThrowAnyException();
    assertViolationExceptionForField(
        () -> scenario.service.create(invalidRequest),
        "when.startTime",
        "start");
  }

  @Test
  void create_shouldAllowNonRecurringEventStartWithRemainderSeconds() {
    val scenario = new Scenario()
        .withCalendarAccess(x -> new CalendarAccessInfo(x.orgId, false));
    val startTime = Instant.now().truncatedTo(ChronoUnit.MINUTES);
    val eventCreateRequest = scenario.buildEventCreateRequest()
        .recurrence(Recurrence.none())
        .when(ModelBuilders.whenTimeSpan()
            .startTime(startTime)
            .endTime(startTime.plusSeconds(300))
            .build())
        .build();
    val requestWithReminderSeconds = scenario.buildEventCreateRequest()
        .recurrence(Recurrence.none())
        .when(ModelBuilders.whenTimeSpan()
            .startTime(startTime.plusSeconds(30))
            .endTime(startTime.plusSeconds(300))
            .build())
        .build();

    assertThatCode(() -> scenario.service.create(eventCreateRequest))
        .doesNotThrowAnyException();
    assertThatCode(() -> scenario.service.create(requestWithReminderSeconds))
        .doesNotThrowAnyException();
  }

  @Test
  void update_shouldSyncWhenAccountIdExists() {
    val scenario = new Scenario()
        .withAccount()
        .withEventAccess(x -> new EventAccessInfo(x.orgId, false))
        .withEventRecurrence(x -> Recurrence.none())
        .withExternalEvent();
    val request = scenario
        .buildEventUpdateRequest()
        .externalId(scenario.externalId.orElseThrow())
        .build();

    assertThatCode(() -> scenario.service.update(request))
        .doesNotThrowAnyException();

    verify(scenario.deps.eventRepoMock).update(argThat(x -> x.id().equals(request.id())));
    verify(scenario.deps.nylasTaskSchedulerMock).exportEventToNylas(
        scenario.accountId.orElseThrow(), request.id());
  }

  @Test
  void update_shouldNotSyncWhenNoAccountId() {
    val scenario = new Scenario()
        .withEventAccess(x -> new EventAccessInfo(x.orgId, false))
        .withEventRecurrence(x -> Recurrence.none());
    val request = scenario.buildEventUpdateRequest().build();

    assertThatCode(() -> scenario.service.update(request))
        .doesNotThrowAnyException();

    verify(scenario.deps.eventRepoMock).update(argThat(x -> x.id().equals(request.id())));
    verifyNoInteractions(scenario.deps.nylasTaskSchedulerMock);
  }

  @Test
  void update_shouldPublishEventChanged() {
    val scenario = new Scenario()
        .withEventAccess(x -> new EventAccessInfo(x.orgId, false));
    val request = scenario.buildEventUpdateRequest().build();

    scenario.service.update(request);

    scenario.deps.eventPublisherMock.verify()
        .noEventCreated()
        .hasEventUpdated(scenario.eventId)
        .noEventDeleted();
  }

  @Test
  void update_shouldNotSyncOrPublishEventsWhenNoChanges() {
    val scenario = new Scenario()
        .withAccount()
        .withExternalEvent();
    val event = ModelBuilders.eventWithTestData()
        .id(scenario.eventId)
        .externalId(scenario.externalId.orElseThrow())
        .calendarId(scenario.calendarId)
        .orgId(scenario.orgId)
        .participants(TestData.participantList(2))
        .build();
    val request = scenario.buildEventUpdateRequest()
        .externalId(event.externalId())
        .icalUid(event.icalUid())
        .title(event.title())
        .description(event.description())
        .location(event.location())
        .when(event.when())
        .status(event.status())
        .isBusy(event.isBusy())
        .isReadOnly(event.isReadOnly())
        .owner(event.owner())
        .participants(event.participants()
            .stream()
            .map(x -> ParticipantRequest.builder().name(x.name()).email(x.email()).build())
            .toList())
        .build();

    when(scenario.deps.eventRepoMock.get(scenario.eventId)).thenReturn(event);

    scenario.service.update(request);

    verify(scenario.deps.eventRepoMock).update(argThat(x -> x.id().equals(request.id())));
    verifyNoInteractions(scenario.deps.nylasTaskSchedulerMock);
    scenario.deps.eventPublisherMock.verify().noEventChangedOfAnyType();
  }

  @Test
  void update_shouldThrowForEventInDifferentOrg() {
    val scenario = new Scenario()
        .withAccount()
        .withEventAccess(x -> new EventAccessInfo(x.orgId, false));
    val request = scenario.buildEventUpdateRequest()
        .orgId(new OrgId("different org"))
        .orgId(new OrgId("different org"))
        .build();

    assertThatCode(() -> scenario.service.update(request))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Event");

    verify(scenario.deps.eventRepoMock, never()).update(any());
    verifyNoInteractions(scenario.deps.nylasTaskSchedulerMock);
  }

  @Test
  void update_shouldThrowForReadOnlyEvent() {
    val scenario = new Scenario()
        .withAccount()
        .withEventAccess(x -> new EventAccessInfo(x.orgId, true));
    val request = scenario.buildEventUpdateRequest().build();

    assertThatCode(() -> scenario.service.update(request))
        .isInstanceOf(ReadOnlyException.class)
        .hasMessageContaining("Event");

    verify(scenario.deps.eventRepoMock, never()).update(any());
    verifyNoInteractions(scenario.deps.nylasTaskSchedulerMock);
  }

  @Test
  void update_shouldThrowForRecurringMasterWithoutRecurrence() {
    val scenario = new Scenario()
        .withEventAccess(x -> new EventAccessInfo(x.orgId, false))
        .withEventRecurrence(x -> TestData.recurrenceMaster());
    val request = scenario.buildEventUpdateRequest()
        .recurrence(null)
        .build();

    assertViolationExceptionForField(
        () -> scenario.service.update(request),
        "recurrence",
        "recurrence master");

    verify(scenario.deps.eventRepoMock, never()).update(any());
    verifyNoInteractions(scenario.deps.nylasTaskSchedulerMock);
  }

  @Test
  void update_shouldThrowForRecurringInstanceWithRecurrence() {
    val scenario = new Scenario()
        .withEventAccess(x -> new EventAccessInfo(x.orgId, false))
        .withEventRecurrence(x -> TestData.recurrenceInstance());
    val request = scenario.buildEventUpdateRequest()
        .recurrence(TestData.recurrenceMaster().getMaster())
        .build();

    assertViolationExceptionForField(
        () -> scenario.service.update(request),
        "recurrence",
        "recurrence instance");

    verify(scenario.deps.eventRepoMock, never()).update(any());
    verifyNoInteractions(scenario.deps.nylasTaskSchedulerMock);
  }

  @Test
  void update_shouldValidateAndFail() {
    val scenario = new Scenario();
    val request = scenario.buildEventUpdateRequest()
        .when(ModelBuilders.whenDateSpan().startDate(TestData.localDate()).build()) // invalid
        .build();

    assertThatValidationFails(() -> scenario.service.update(request));
  }

  @Test
  void update_shouldValidateEventsActivePeriod() {
    val scenario = new Scenario();
    val activePeriodEnd = scenario.eventsConfig.activePeriod().current().endAtUtcOffset();
    val request = scenario.buildEventUpdateRequest()
        .when(new When.Date(activePeriodEnd.toLocalDate().plusDays(1)))
        .build();

    assertViolationExceptionForField(
        () -> scenario.service.update(request),
        "when.date",
        "active period");
  }

  @Test
  void update_shouldValidateRecurrenceMasterStartIsWholeMinutes() {
    val scenario = new Scenario()
        .withEventRecurrence(x -> TestData.recurrenceMaster());
    val invalidRequest = scenario.buildEventUpdateRequest()
        .recurrence(TestData.recurrenceMaster().getMaster())
        .when(ModelBuilders.whenTimeSpan()
            .startTime(Instant.now().truncatedTo(ChronoUnit.MINUTES).plusSeconds(30))
            .endTime(Instant.now().plusSeconds(300))
            .build())
        .build();
    assertViolationExceptionForField(
        () -> scenario.service.update(invalidRequest),
        "when.startTime",
        "start");
  }

  @Test
  void update_shouldValidateRecurrenceInstanceStartIsWholeMinutes() {
    val scenario = new Scenario()
        .withEventRecurrence(x -> TestData.recurrenceInstance());
    val invalidRequest = scenario.buildEventUpdateRequest()
        .recurrence(TestData.recurrenceMaster().getMaster())
        .when(ModelBuilders.whenTimeSpan()
            .startTime(Instant.now().truncatedTo(ChronoUnit.MINUTES).plusSeconds(30))
            .endTime(Instant.now().plusSeconds(300))
            .build())
        .build();
    assertViolationExceptionForField(
        () -> scenario.service.update(invalidRequest),
        "when.startTime",
        "start");
  }

  @Test
  void update_shouldAllowNonRecurringEventStartWithRemainderSeconds() {
    val scenario = new Scenario();
    val request = scenario.buildEventUpdateRequest()
        .recurrence(TestData.recurrenceMaster().getMaster())
        .when(ModelBuilders.whenTimeSpan()
            .startTime(Instant.now().truncatedTo(ChronoUnit.MINUTES).plusSeconds(30))
            .endTime(Instant.now().plusSeconds(300))
            .build())
        .build();
    assertThatCode(() -> scenario.service.update(request))
        .doesNotThrowAnyException();
  }

  @Test
  void delete_shouldSyncWhenAccountIdExists() {
    val scenario = new Scenario()
        .withAccount()
        .withEventAccess(x -> new EventAccessInfo(x.orgId, false))
        .withExternalEvent();

    assertThatCode(() -> scenario.service.delete(scenario.buildEventRequest().build()))
        .doesNotThrowAnyException();

    verify(scenario.deps.eventRepoMock).delete(scenario.eventId);
    verify(scenario.deps.nylasTaskSchedulerMock)
        .deleteEventFromNylas(any(), any());
  }

  @Test
  void delete_shouldAllowReadOnlyEvent() {
    val scenario = new Scenario()
        .withAccount()
        .withEventAccess(x -> new EventAccessInfo(x.orgId, true))
        .withExternalEvent();

    assertThatCode(() -> scenario.service.delete(scenario.buildEventRequest().build()))
        .doesNotThrowAnyException();

    verify(scenario.deps.eventRepoMock).delete(scenario.eventId);
    verify(scenario.deps.nylasTaskSchedulerMock)
        .deleteEventFromNylas(any(), any());
  }

  @Test
  void delete_shouldNotSyncWhenNoAccountId() {
    val scenario = new Scenario()
        .withEventAccess(x -> new EventAccessInfo(x.orgId, false));

    assertThatCode(() -> scenario.service.delete(scenario.buildEventRequest().build()))
        .doesNotThrowAnyException();

    verify(scenario.deps.eventRepoMock).delete(scenario.eventId);
    verifyNoInteractions(scenario.deps.nylasTaskSchedulerMock);
  }

  @Test
  void delete_shouldPublishEventChanged() {
    val scenario = new Scenario()
        .withEventAccess(x -> new EventAccessInfo(x.orgId, false));
    val request = scenario.buildEventRequest().build();

    scenario.service.delete(request);

    scenario.deps.eventPublisherMock.verify()
        .noEventCreated()
        .noEventUpdated()
        .hasEventDeleted(request.dataSource(), scenario.eventId);
  }

  @Test
  void delete_shouldPublishEventChangedForRecurrenceMasterAndInstances() {
    val instanceIds = Stream.generate(EventId::create).limit(2).toList();
    val scenario = new Scenario()
        .withEventAccess(x -> new EventAccessInfo(x.orgId, false))
        .withEventRecurrence(x -> TestData.recurrenceMaster())
        .withRecurrenceInstanceIdPairs(
            instanceIds.stream()
                .map(x -> Pair.of(x, Optional.<EventExternalId>empty()))
                .toList());
    val request = scenario.buildEventRequest().build();

    scenario.service.delete(request);

    scenario.deps.eventPublisherMock.verify()
        .noEventCreated()
        .noEventUpdated()
        .hasEventDeleted(
            request.dataSource(),
            Arrays.concat(
                new EventId[]{scenario.eventId}, // master
                instanceIds.toArray(EventId[]::new))); // instances
  }

  @Test
  void delete_shouldThrowForEventInDifferentOrg() {
    val scenario = new Scenario()
        .withAccount()
        .withEventAccess(x -> new EventAccessInfo(x.orgId, false));

    assertThatCode(() -> scenario.service.delete(
        scenario.buildEventRequest().orgId(TestData.orgId()).build()))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Event");

    verify(scenario.deps.eventRepoMock, never()).delete(any());
    verifyNoInteractions(scenario.deps.nylasTaskSchedulerMock);
  }

  @Test
  void delete_shouldValidateRequest() {
    val scenario = new Scenario();
    val invalidDataSource = new DataSource("");

    assertThatValidationFails(() -> scenario.service.delete(
        scenario.buildEventRequest().dataSource(invalidDataSource).build()))
        .hasMessageContaining(EventRequest.class.getSimpleName());
  }

  @Test
  void checkin_checkout_shouldValidateEventRequest() {
    val scenario = new Scenario();
    val invalidDataSource = new DataSource("");
    val request = scenario.buildEventRequest().dataSource(invalidDataSource).build();

    assertThatValidationFails(() -> scenario.service.checkin(request))
        .hasMessageContaining(EventRequest.class.getSimpleName());

    assertThatValidationFails(() -> scenario.service.checkout(request))
        .hasMessageContaining(EventRequest.class.getSimpleName());
  }

  @Test
  void checkin_checkout_forInternalEvent_shouldUpdateDb() {
    val scenario = new Scenario()
        .withEventAccess(x -> new EventAccessInfo(x.orgId, false));
    val request = scenario.buildEventRequest().build();

    scenario.service.checkin(request);
    verify(scenario.deps.eventRepoMock).checkin(scenario.eventId, request.dataSource());
    verifyNoInteractions(scenario.deps.nylasTaskSchedulerMock);

    scenario.service.checkout(request);
    verify(scenario.deps.eventRepoMock).checkin(scenario.eventId, request.dataSource());
    verifyNoInteractions(scenario.deps.nylasTaskSchedulerMock);
  }

  @Test
  void checkin_checkout_forWritableExternalEvent_shouldUpdateDbAndSyncToNylas() {
    val scenario = new Scenario()
        .withAccount()
        .withEventAccess(x -> new EventAccessInfo(x.orgId, false))
        .withExternalEvent();
    val request = scenario.buildEventRequest().build();

    scenario.service.checkin(request);
    verify(scenario.deps.eventRepoMock).checkin(scenario.eventId, request.dataSource());
    verify(scenario.deps.nylasTaskSchedulerMock).exportEventToNylas(
        scenario.accountId.orElseThrow(),
        scenario.eventId);

    scenario.service.checkout(request);
    verify(scenario.deps.eventRepoMock).checkout(scenario.eventId, request.dataSource());
    verify(scenario.deps.nylasTaskSchedulerMock, times(2)).exportEventToNylas(
        scenario.accountId.orElseThrow(),
        scenario.eventId);
  }

  @Test
  void checkin_checkout_forReadOnlyExternalEvent_shouldUpdateDbAndSkipSync() {
    val scenario = new Scenario()
        .withAccount()
        .withEventAccess(x -> new EventAccessInfo(x.orgId, true))
        .withExternalEvent();
    val request = scenario.buildEventRequest().build();

    scenario.service.checkin(request);
    verify(scenario.deps.eventRepoMock).checkin(scenario.eventId, request.dataSource());
    verifyNoInteractions(scenario.deps.nylasTaskSchedulerMock);

    scenario.service.checkout(request);
    verify(scenario.deps.eventRepoMock).checkout(scenario.eventId, request.dataSource());
    verifyNoInteractions(scenario.deps.nylasTaskSchedulerMock);
  }

  @Test
  void checkin_checkout_shouldPublishEventChanged() {
    val scenario = new Scenario()
        .withEventAccess(x -> new EventAccessInfo(x.orgId, true));
    val request = scenario.buildEventRequest().build();

    scenario.service.checkin(request);
    scenario.deps.eventPublisherMock.verify()
        .noEventCreated()
        .hasEventUpdated(scenario.eventId)
        .noEventDeleted()
        .resetMock();

    scenario.service.checkout(request);
    scenario.deps.eventPublisherMock.verify()
        .noEventCreated()
        .hasEventUpdated(scenario.eventId)
        .noEventDeleted();
  }

  /**
   * Helper for setting up test scenarios for the service.
   */
  private static class Scenario {
    private final OrgId orgId = TestData.orgId();
    private final EventsConfig eventsConfig = TestData.eventsConfig();
    private Optional<AccountId> accountId = Optional.empty();
    private final CalendarId calendarId = CalendarId.create();
    private Optional<CalendarAccessInfo> calendarAccessInfo = Optional.empty();
    private final EventId eventId = EventId.create();
    private Optional<EventExternalId> externalId = Optional.empty();
    private Optional<EventAccessInfo> eventAccessInfo = Optional.empty();
    private Optional<Recurrence> recurrence = Optional.empty();
    private final List<Pair<EventId, Optional<EventExternalId>>> recurrenceInstanceIdPairs =
        new ArrayList<>();
    private final Dependencies deps = new Dependencies(
        mock(CalendarRepository.class),
        mock(EventRepository.class),
        mock(NylasTaskScheduler.class),
        ValidatorWrapperFactory.createRealInstance(),
        new EventPublisherMock(),
        mock(ConferencingService.class));
    private final EventService service = new EventService(
        deps.calendarRepoMock(),
        deps.eventRepoMock(),
        deps.nylasTaskSchedulerMock(),
        deps.validator,
        eventsConfig,
        deps.eventPublisherMock,
        deps.conferencingServiceMock);

    public Scenario() {
      when(deps.calendarRepoMock.tryGetAccessInfo(any(CalendarId.class)))
          .then(inv -> calendarAccessInfo.filter(x -> calendarId.equals(inv.getArgument(0))));

      when(deps.calendarRepoMock.getAccountId(any(CalendarId.class)))
          .then(inv -> accountId.filter(x -> calendarId.equals(inv.getArgument(0))));

      when(deps.eventRepoMock.getAccountId(any(EventId.class)))
          .then(inv -> accountId.filter(x -> eventId.equals(inv.getArgument(0))));

      when(deps.eventRepoMock.getAccountAndExternalIds(any(EventId.class)))
          .then(inv -> accountId
              .filter(x -> eventId.equals(inv.getArgument(0)))
              .flatMap(x -> externalId.map(y -> Pair.of(accountId, externalId)))
              .orElseGet(() -> Pair.of(Optional.empty(), Optional.empty())));

      when(deps.eventRepoMock.getAccessInfo(any(EventId.class)))
          .then(inv -> eventAccessInfo
              .filter(x -> eventId.equals(inv.getArgument(0)))
              .orElseThrow(() -> NotFoundException.ofClass(Event.class)));

      when(deps.eventRepoMock.getRecurrence(any(EventId.class)))
          .then(inv -> recurrence
              .filter(x -> eventId.equals(inv.getArgument(0)))
              .orElseThrow(() -> NotFoundException.ofClass(Event.class)));

      when(deps.eventRepoMock.getCoreIds(any(EventId.class)))
          .then(inv -> Optional.ofNullable(inv.getArgument(0))
              .filter(x -> x.equals(eventId))
              .map(x -> new CoreIds(eventId, externalId, calendarId, orgId))
              .orElseThrow(() -> NotFoundException.ofClass(Event.class)));

      when(deps.eventRepoMock.listRecurrenceInstanceIdPairs(any(EventId.class)))
          .then(inv -> Optional.ofNullable(inv.getArgument(0))
              .filter(x -> x.equals(eventId))
              .map(x -> recurrenceInstanceIdPairs.stream())
              .orElse(Stream.empty()));

      when(deps.eventRepoMock.get(any(EventId.class)))
          .then(inv -> Optional
              .ofNullable(inv.getArgument(0))
              .filter(eventId::equals)
              .map(x -> ModelBuilders.eventWithTestData()
                  .id(eventId)
                  .calendarId(calendarId)
                  .orgId(orgId)
                  .externalId(externalId.orElse(null))
                  .recurrence(recurrence.orElse(null))
                  .isReadOnly(eventAccessInfo.map(y -> y.isReadOnly()).orElse(false))
                  .build())
              .orElseThrow(() -> NotFoundException.ofClass(Event.class)));
    }

    public Scenario withAccount() {
      accountId = Optional.of(new AccountId(UUID.randomUUID().toString()));
      return this;
    }

    public Scenario withCalendarAccess(Function<Scenario, CalendarAccessInfo> setter) {
      calendarAccessInfo = Optional.ofNullable(setter.apply(this));
      return this;
    }

    public Scenario withEventAccess(Function<Scenario, EventAccessInfo> setter) {
      eventAccessInfo = Optional.ofNullable(setter.apply(this));
      return this;
    }

    public Scenario withEventRecurrence(Function<Scenario, Recurrence> setter) {
      recurrence = Optional.ofNullable(setter.apply(this));
      return this;
    }

    public Scenario withRecurrenceInstanceIdPairs(
        Collection<Pair<EventId, Optional<EventExternalId>>> pairs) {
      recurrenceInstanceIdPairs.addAll(pairs);
      return this;
    }

    public Scenario withExternalEvent() {
      externalId = Optional.of(new EventExternalId(UUID.randomUUID().toString()));
      val pair = Pair.of(accountId, externalId);
      when(deps.eventRepoMock().getAccountAndExternalIds(eventId)).thenReturn(pair);
      when(deps.eventRepoMock().getExternalId(eventId)).thenReturn(externalId);
      return this;
    }

    public EventCreateRequest.Builder buildEventCreateRequest() {
      return ModelBuilders.eventCreateRequestWithTestData()
          .id(eventId)
          .orgId(orgId)
          .calendarId(calendarId);
    }

    public EventUpdateRequest.Builder buildEventUpdateRequest() {
      return ModelBuilders.eventUpdateRequestWithTestData()
          .id(eventId)
          .orgId(orgId)
          .title("updated");
    }

    public ModelBuilders.EventRequestBuilder buildEventRequest() {
      return ModelBuilders.eventRequest()
          .eventId(eventId)
          .orgId(orgId)
          .dataSource(DataSource.fromApi(TestData.uuidString()));
    }

    private record Dependencies(
        CalendarRepository calendarRepoMock,
        EventRepository eventRepoMock,
        NylasTaskScheduler nylasTaskSchedulerMock,
        ValidatorWrapper validator,
        EventPublisherMock eventPublisherMock,
        ConferencingService conferencingServiceMock) {
    }
  }
}
