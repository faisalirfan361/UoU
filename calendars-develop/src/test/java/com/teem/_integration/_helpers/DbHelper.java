package com.UoU._integration._helpers;

import static com.UoU._helpers.TestData.email;
import static com.UoU.infra.jooq.Tables.ACCOUNT;
import static com.UoU.infra.jooq.Tables.SERVICE_ACCOUNT;
import static com.UoU.infra.jooq.tables.Calendar.CALENDAR;
import static com.UoU.infra.jooq.tables.Event.EVENT;
import static com.UoU.infra.jooq.tables.Participant.PARTICIPANT;

import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU.core.Fluent;
import com.UoU.core.Noop;
import com.UoU.core.OrgId;
import com.UoU.core.SecretString;
import com.UoU.core.accounts.AccountCreateRequest;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.AccountRepository;
import com.UoU.core.accounts.ServiceAccountCreateRequest;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.accounts.ServiceAccountRepository;
import com.UoU.core.auth.AuthMethod;
import com.UoU.core.calendars.AvailabilityRepository;
import com.UoU.core.calendars.CalendarCreateRequest;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.calendars.CalendarRepository;
import com.UoU.core.conferencing.ConferencingUserCreateRequest;
import com.UoU.core.conferencing.ConferencingUserId;
import com.UoU.core.conferencing.ConferencingUserRepository;
import com.UoU.core.events.EventCreateRequest;
import com.UoU.core.events.EventExternalId;
import com.UoU.core.events.EventId;
import com.UoU.core.events.EventRepository;
import com.UoU.core.events.ParticipantRequest;
import com.UoU.core.events.ParticipantStatus;
import com.UoU.core.events.When;
import com.UoU.infra.jooq.tables.records.AccountRecord;
import com.UoU.infra.jooq.tables.records.CalendarRecord;
import com.UoU.infra.jooq.tables.records.EventRecord;
import com.UoU.infra.jooq.tables.records.ParticipantRecord;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.Value;
import lombok.val;
import org.jooq.DSLContext;
import org.jooq.exception.NoDataFoundException;
import org.springframework.boot.test.context.TestComponent;

/**
 * Helper for working with the db to setup test data, verify db state, etc.
 *
 * <p>Some db operations can be done via services or repos as well, but there may sometimes be
 * extra logic there that's not needed for tests. This has some operations that are more low-level
 * or that are missing from accountRepoMock functionality. In any case, this gathers common stuff in
 * one place.
 */
@TestComponent
@Value
public class DbHelper {
  DSLContext dsl;
  AccountRepository accountRepo;
  AvailabilityRepository availabilityRepo;
  EventRepository eventRepo;
  ServiceAccountRepository serviceAccountRepo;
  CalendarRepository calendarRepo;
  ConferencingUserRepository conferencingUserRepo;

  public AccountRecord getAccount(AccountId id) {
    return dsl
        .selectFrom(ACCOUNT)
        .where(ACCOUNT.ID.eq(id.value()))
        .fetchSingle();
  }

  public AccountId createAccount(OrgId orgId) {
    return createAccount(orgId, x -> Noop.because("no customizer"));
  }

  public AccountId createAccount(
      OrgId orgId, Consumer<AccountCreateRequest.Builder> customizer) {

    var id = TestData.accountId();
    var request = Fluent.of(AccountCreateRequest.builder()
            .id(id)
            .orgId(orgId)
            .email(TestData.email())
            .name("Test Account")
            .authMethod(AuthMethod.GOOGLE_OAUTH)
            .accessToken(new SecretString("x")))
        .also(customizer)
        .get()
        .build();
    accountRepo.create(request);

    return id;
  }

  public void updateAccount(AccountId id, Consumer<AccountRecord> updater) {
    val record = dsl.newRecord(ACCOUNT).setId(id.value());
    updater.accept(record);
    record.changed(ACCOUNT.ID, false);
    dsl.executeUpdate(record);
  }

  public AccountId createSubaccount(OrgId orgId, ServiceAccountId serviceAccountId) {
    return createAccount(orgId, x -> x
        .serviceAccountId(serviceAccountId)
        .authMethod(AuthMethod.MS_OAUTH_SA));
  }

  public ServiceAccountId createServiceAccount(OrgId orgId) {
    var id = ServiceAccountId.create();
    serviceAccountRepo.create(ServiceAccountCreateRequest.builder()
        .id(id)
        .orgId(orgId)
        .email(TestData.email())
        .settings(Map.of("microsoft_refresh_token", "123"))
        .authMethod(AuthMethod.MS_OAUTH_SA)
        .build());

    return id;
  }

  public ServiceAccountId createServiceAccount(
      OrgId orgId, Consumer<ServiceAccountCreateRequest.Builder> customizer) {

    var request = Fluent.of(ServiceAccountCreateRequest.builder()
            .id(TestData.serviceAccountId())
            .orgId(orgId)
            .email(TestData.email())
            .settings(Map.of("microsoft_refresh_token", "123"))
            .authMethod(AuthMethod.MS_OAUTH_SA))
        .also(customizer)
        .get()
        .build();
    serviceAccountRepo.create(request);

    return request.id();
  }

  public CalendarId createCalendar(OrgId orgId) {
    return createCalendar(orgId, x -> { /* noop */ });
  }

  public CalendarId createCalendar(OrgId orgId, AccountId accountId) {
    return createCalendar(orgId, x -> x.accountId(accountId));
  }

  public CalendarId createCalendar(
      OrgId orgId, AccountId accountId, CalendarExternalId externalId) {
    return createCalendar(orgId, x -> x.accountId(accountId).externalId(externalId));
  }

  public CalendarId createCalendar(
      OrgId orgId, Consumer<CalendarCreateRequest.Builder> customizer) {

    var calendar = Fluent
        .of(CalendarCreateRequest.builder()
            .id(CalendarId.create())
            .orgId(orgId)
            .name("Test calendar")
            .timezone("America/Denver")
            .isReadOnly(false))
        .also(customizer)
        .get()
        .build();
    calendarRepo.create(calendar);

    return calendar.id();
  }

  public Stream<CalendarId> createCalendars(OrgId orgId, AccountId accountId) {
    return Stream.generate(() -> createCalendar(orgId, accountId));
  }

  public List<CalendarId> createCalendars(
      OrgId orgId, AccountId accountId, Collection<CalendarExternalId> externalIds) {
    return externalIds.stream().map(x -> createCalendar(orgId, accountId, x)).toList();
  }

  public CalendarId createCalendarWithAccount(
      OrgId orgId,
      Consumer<AccountCreateRequest.Builder> accountCustomizer,
      Consumer<CalendarCreateRequest.Builder> calendarCustomizer) {

    val accountId = createAccount(orgId, accountCustomizer);
    return createCalendar(orgId, x -> calendarCustomizer.accept(x.accountId(accountId)));
  }

  public CalendarId createCalendarWithAccount(
      OrgId orgId,
      Consumer<AccountCreateRequest.Builder> accountCustomizer) {

    val accountId = createAccount(orgId, accountCustomizer);
    return createCalendar(orgId, accountId);
  }

  public CalendarRecord getCalendar(CalendarId calendarId) {
    return dsl
        .selectFrom(CALENDAR)
        .where(CALENDAR.ID.eq(calendarId.value()))
        .fetchSingle();
  }

  public CalendarRecord getCalendarByExternalId(CalendarExternalId calendarExternalId) {
    return dsl
        .selectFrom(CALENDAR)
        .where(CALENDAR.EXTERNAL_ID.eq(calendarExternalId.value()))
        .fetchSingle();
  }

  public Stream<CalendarRecord> getCalendars(AccountId accountId) {
    return dsl
        .selectFrom(CALENDAR)
        .where(CALENDAR.ACCOUNT_ID.eq(accountId.value()))
        .fetchStream();
  }

  public EventId createEvent(OrgId orgId, CalendarId calendarId) {
    return createEvent(orgId, calendarId, x -> Noop.because("no customizer"));
  }

  public EventId createEvent(OrgId orgId, CalendarId calendarId, EventExternalId externalId) {
    return createEvent(orgId, calendarId, x -> x.externalId(externalId));
  }

  public EventId createEvent(OrgId orgId, CalendarId calendarId, int participantCount) {
    return createEvent(
        orgId,
        calendarId,
        x -> x.participants(createParticipantRequests().limit(participantCount).toList()));
  }

  public EventId createEvent(
      OrgId orgId,
      CalendarId calendarId,
      Consumer<EventCreateRequest.Builder> customizer) {

    var event = Fluent
        .of(ModelBuilders.eventCreateRequestWithTestData()
            .id(EventId.create())
            .orgId(orgId)
            .calendarId(calendarId))
        .also(customizer)
        .get()
        .build();
    eventRepo.create(event);

    return event.id();
  }

  public Stream<EventId> createEvents(OrgId orgId, CalendarId calendarId) {
    return Stream.generate(() -> createEvent(orgId, calendarId));
  }

  public Stream<EventId> createEvents(OrgId orgId, CalendarId calendarId, int participantCount) {
    return Stream.generate(() -> createEvent(orgId, calendarId, participantCount));
  }

  public List<EventId> createEvents(OrgId orgId, CalendarId calendarId, When... whens) {
    return Stream.of(whens).map(when -> createEvent(orgId, calendarId, x -> x.when(when))).toList();
  }

  @SafeVarargs
  public final Stream<EventId> createEvents(
      OrgId orgId,
      CalendarId calendarId,
      Consumer<EventCreateRequest.Builder>... customizers) {
    return Stream.of(customizers).map(x -> createEvent(orgId, calendarId, x));
  }

  public EventRecord getEvent(EventId id) {
    return dsl
        .selectFrom(EVENT)
        .where(EVENT.ID.eq(id.value()))
        .fetchSingle();
  }

  public EventRecord getEventByExternalId(EventExternalId externalId) {
    return dsl
        .selectFrom(EVENT)
        .where(EVENT.EXTERNAL_ID.eq(externalId.value()))
        .fetchSingle();
  }

  public EventId getEventIdByExternalId(EventExternalId externalId) {
    return new EventId(dsl
        .select(EVENT.ID)
        .from(EVENT)
        .where(EVENT.EXTERNAL_ID.eq(externalId.value()))
        .fetchSingle()
        .value1());
  }

  public Optional<EventRecord> tryGetEventByExternalId(EventExternalId externalId) {
    try {
      return Optional.of(getEventByExternalId(externalId));
    } catch (NoDataFoundException ex) {
      return Optional.empty();
    }
  }

  public List<ParticipantRecord> getParticipants(EventId id) {
    return dsl
        .selectFrom(PARTICIPANT)
        .where(PARTICIPANT.EVENT_ID.eq(id.value()))
        .stream()
        .toList();
  }

  public Stream<ParticipantRequest> createParticipantRequests() {
    return Stream
        .iterate(1, x -> x + 1)
        .map((x) -> ParticipantRequest.builder()
            .name("Participant " + x)
            .email(email())
            .status(ParticipantStatus.NO_REPLY)
            .comment("Comment")
            .build());
  }

  /**
   * Deletes all service accounts and dependent entities.
   */
  public void resetServiceAccounts() {
    val calendarIds = dsl
        .select(CALENDAR.ID)
        .from(CALENDAR)
        .join(ACCOUNT).on(ACCOUNT.ID.eq(CALENDAR.ACCOUNT_ID))
        .where(ACCOUNT.SERVICE_ACCOUNT_ID.isNotNull())
        .fetchSet(CALENDAR.ID);
    val selectEventIds = dsl.select(EVENT.ID).from(EVENT).where(EVENT.CALENDAR_ID.in(calendarIds));

    dsl.deleteFrom(PARTICIPANT).where(PARTICIPANT.EVENT_ID.in(selectEventIds));
    dsl.deleteFrom(EVENT).where(EVENT.ID.in(selectEventIds));
    dsl.deleteFrom(ACCOUNT).where(ACCOUNT.SERVICE_ACCOUNT_ID.isNotNull());
    dsl.deleteFrom(SERVICE_ACCOUNT);
  }

  /**
   * Deletes all test calendars and dependent entities.
   */
  public void resetCalendars() {
    dsl.deleteFrom(PARTICIPANT).execute();
    dsl.deleteFrom(EVENT).execute();
    dsl.deleteFrom(CALENDAR).execute();
  }

  public ConferencingUserId createConferencingUser(OrgId orgId, AuthMethod authMethod) {
    return createConferencingUser(orgId, x -> x.authMethod(authMethod));
  }

  public ConferencingUserId createConferencingUser(
      OrgId orgId, Consumer<ConferencingUserCreateRequest.Builder> customizer) {

    val builder = ConferencingUserCreateRequest.builder()
        .id(ConferencingUserId.create())
        .orgId(orgId)
        .name("Test User")
        .email(TestData.email())
        .refreshToken(new SecretString("test-refresh"))
        .accessToken(new SecretString("test-access"))
        .expireAt(Instant.now().plus(15, ChronoUnit.MINUTES))
        .authMethod(AuthMethod.CONF_TEAMS_OAUTH);
    customizer.accept(builder);

    val request = builder.build();
    conferencingUserRepo.create(request);

    return request.id();
  }
}
