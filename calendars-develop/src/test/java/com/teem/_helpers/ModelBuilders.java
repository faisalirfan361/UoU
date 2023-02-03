package com.UoU._helpers;

import com.UoU.core.OrgId;
import com.UoU.core.TimeSpan;
import com.UoU.core.accounts.Account;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.ServiceAccount;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.accounts.SyncState;
import com.UoU.core.auth.AuthCode;
import com.UoU.core.auth.AuthCodeCreateRequest;
import com.UoU.core.auth.AuthMethod;
import com.UoU.core.auth.SubaccountAuthRequest;
import com.UoU.core.calendars.AvailabilityRequest;
import com.UoU.core.calendars.Calendar;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.calendars.InternalCalendarBatchCreateRequest;
import com.UoU.core.conferencing.ConferencingUser;
import com.UoU.core.conferencing.ConferencingUserId;
import com.UoU.core.diagnostics.DiagnosticRequest;
import com.UoU.core.events.DataSource;
import com.UoU.core.events.Event;
import com.UoU.core.events.EventCreateRequest;
import com.UoU.core.events.EventExternalId;
import com.UoU.core.events.EventId;
import com.UoU.core.events.EventRequest;
import com.UoU.core.events.EventUpdateRequest;
import com.UoU.core.events.Owner;
import com.UoU.core.events.Participant;
import com.UoU.core.events.ParticipantStatus;
import com.UoU.core.events.Recurrence;
import com.UoU.core.events.When;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.val;

/**
 * Builders that make creating models for tests easier.
 *
 * <p>Note: Most builders won't exist in the actual model code because we won't ever need to create
 * most models in our own non-test code. Rather, we use mappers to rehydrate from JSON or db
 * values. However, if we do need a builder for a certain model in our non-test code, put the
 * builder in the model (put @lombok.Builder on the class or on the record's compact constructor).
 */
public class ModelBuilders {

  @Builder(builderMethodName = "authCode")
  private static AuthCode buildAuthCode(
      UUID code,
      OrgId orgId,
      String redirectUri
  ) {
    return new AuthCode(code, orgId, redirectUri);
  }

  public static AuthCodeBuilder authCodeWithTestData() {
    return authCode()
        .code(UUID.randomUUID())
        .orgId(TestData.orgId())
        .redirectUri("https://example.com");
  }

  @Builder(builderMethodName = "authCodeCreateRequest")
  private static AuthCodeCreateRequest buildAuthCodeCreateRequest(
      UUID code,
      OrgId orgId,
      Duration expiration,
      String redirectUri
  ) {
    return new AuthCodeCreateRequest(code, orgId, expiration, redirectUri);
  }

  public static AuthCodeCreateRequestBuilder authCodeCreateRequestWithTestData() {
    return authCodeCreateRequest()
        .code(UUID.randomUUID())
        .orgId(TestData.orgId())
        .expiration(Duration.ofSeconds(60))
        .redirectUri("https://example.com");
  }

  @Builder(builderMethodName = "account")
  private static Account buildAccount(
      AccountId id,
      ServiceAccountId serviceAccountId,
      OrgId orgId,
      String email,
      String name,
      SyncState syncState,
      AuthMethod authMethod,
      Instant createdAt,
      Instant updatedAt) {
    return new Account(
        id, serviceAccountId, orgId, email, name, syncState, authMethod, createdAt, updatedAt);
  }

  public static AccountBuilder accountWithTestData() {
    return account()
        .id(TestData.accountId())
        .orgId(TestData.orgId())
        .name("test")
        .email(TestData.email())
        .authMethod(AuthMethod.GOOGLE_OAUTH)
        .syncState(SyncState.RUNNING)
        .createdAt(Instant.now());
  }

  @Builder(builderMethodName = "subaccountAuthRequest")
  private static SubaccountAuthRequest buildSubaccountAuthRequest(
      ServiceAccountId serviceAccountId,
      OrgId orgId,
      String email,
      String name
  ) {
    return new SubaccountAuthRequest(serviceAccountId, orgId, name, email);
  }

  @Builder(builderMethodName = "serviceAccount")
  private static ServiceAccount buildServiceAccount(
      ServiceAccountId id,
      OrgId orgId,
      String email,
      AuthMethod authMethod,
      Instant createdAt,
      Instant updatedAt) {
    return new ServiceAccount(
        id, orgId, email, authMethod, createdAt, updatedAt);
  }

  public static ServiceAccountBuilder serviceAccountWithTestData() {
    return serviceAccount()
        .id(TestData.serviceAccountId())
        .email(TestData.email())
        .authMethod(AuthMethod.MS_OAUTH_SA)
        .createdAt(Instant.now())
        .orgId(TestData.orgId());
  }

  @Builder(builderMethodName = "calendar")
  private static Calendar buildCalendar(
      CalendarId id,
      CalendarExternalId externalId,
      AccountId accountId,
      OrgId orgId,
      String name,
      boolean isReadOnly,
      String timezone,
      Instant createdAt,
      Instant updatedAt) {
    return new Calendar(
        id, externalId, accountId, orgId, name, isReadOnly, timezone, createdAt, updatedAt);
  }

  public static CalendarBuilder calendarWithTestData() {
    return ModelBuilders.calendar()
        .id(CalendarId.create())
        .externalId(TestData.calendarExternalId())
        .orgId(TestData.orgId())
        .accountId(TestData.accountId())
        .isReadOnly(false)
        .name("Test " + TestData.uuidString())
        .createdAt(Instant.now());
  }

  @Builder(builderMethodName = "internalCalendarBatchCreateRequest")
  private static InternalCalendarBatchCreateRequest buildInternalCalendarBatchCreateRequest(
      OrgId orgId,
      String namePattern,
      String timezone,
      Integer start,
      Integer end,
      Integer increment,
      boolean isDryRun) {
    return new InternalCalendarBatchCreateRequest(
        orgId, namePattern, timezone, start, end, increment, isDryRun);
  }

  public static InternalCalendarBatchCreateRequestBuilder
      internalCalendarBatchCreateRequestWithTestData() {
    return internalCalendarBatchCreateRequest()
        .orgId(TestData.orgId())
        .namePattern("Test {n}")
        .start(100)
        .end(150)
        .increment(10)
        .timezone("UTC");
  }

  public static EventCreateRequest.Builder eventCreateRequestWithTestData() {
    return EventCreateRequest.builder()
        .id(EventId.create())
        .orgId(TestData.orgId())
        .calendarId(CalendarId.create())
        .title("title")
        .description("description")
        .location("location")
        .when(TestData.whenTimeSpan());
  }

  public static EventUpdateRequest.Builder eventUpdateRequestWithTestData() {
    return EventUpdateRequest.builder()
        .id(EventId.create())
        .orgId(TestData.orgId())
        .externalId(TestData.eventExternalId())
        .title("title")
        .description("description")
        .location("location")
        .when(TestData.whenDate());
  }

  @Builder(builderMethodName = "eventRequest")
  private static EventRequest buildEventRequest(
      EventId eventId,
      OrgId orgId,
      DataSource dataSource) {
    return new EventRequest(eventId, orgId, dataSource);
  }

  @Builder(builderMethodName = "event")
  private static Event buildEvent(
      EventId id,
      EventExternalId externalId,
      OrgId orgId,
      String icalUid,
      CalendarId calendarId,
      String title,
      String description,
      String location,
      When when,
      Recurrence recurrence,
      Event.Status status,
      boolean isBusy,
      boolean isReadOnly,
      Instant checkinAt,
      Instant checkoutAt,
      Owner owner,
      List<Participant> participants,
      Instant createdAt,
      DataSource createdFrom,
      Instant updatedAt,
      DataSource updatedFrom
  ) {
    return new Event(
        id, externalId, icalUid, orgId, calendarId, title, description, location, when,
        recurrence, status, isBusy, isReadOnly, checkinAt, checkoutAt, owner,
        participants, createdAt, createdFrom, updatedAt, updatedFrom);
  }

  public static EventBuilder eventWithTestData() {
    return event()
        .id(EventId.create())
        .externalId(TestData.eventExternalId())
        .orgId(TestData.orgId())
        .icalUid(TestData.uuidString())
        .calendarId(CalendarId.create())
        .title("title")
        .description("description")
        .location("location")
        .when(TestData.whenTimeSpan())
        .createdAt(TestData.instant().minus(1, ChronoUnit.DAYS))
        .updatedAt(TestData.instant());
  }

  @Builder(builderMethodName = "participant")
  private static Participant buildParticipant(
      String name,
      String email,
      ParticipantStatus status,
      String comment
  ) {
    return new Participant(name, email, status, comment);
  }

  @Builder(builderMethodName = "whenTimeSpan")
  private static When.TimeSpan buildWhenTimeSpan(
      Instant startTime,
      Instant endTime) {
    return new When.TimeSpan(startTime, endTime);
  }

  @Builder(builderMethodName = "whenDateSpan")
  private static When.DateSpan buildWheDateSpan(
      LocalDate startDate,
      LocalDate endDate) {
    return new When.DateSpan(startDate, endDate);
  }

  @Builder(builderMethodName = "availabilityRequest")
  private static AvailabilityRequest buildAvailabilityRequest(
      OrgId orgId,
      Set<CalendarId> calendarIds,
      TimeSpan timeSpan) {
    return new AvailabilityRequest(orgId, calendarIds, timeSpan);
  }

  @Builder(builderMethodName = "diagnosticRequest")
  private static DiagnosticRequest buildDiagnosticRequest(
      CalendarId calendarId,
      OrgId orgId,
      String callbackUri
  ) {
    return new DiagnosticRequest(calendarId, orgId, callbackUri);
  }

  @Builder(builderMethodName = "conferencingUser")
  private static ConferencingUser buildConferencingUser(
      ConferencingUserId id,
      OrgId orgId,
      String email,
      String name,
      AuthMethod authMethod,
      Instant expireAt,
      Instant createdAt,
      Instant updatedAt
  ) {
    return new ConferencingUser(id, orgId, email, name, authMethod, expireAt, createdAt, updatedAt);
  }

  public static ConferencingUserBuilder conferencingUserWithTestData() {
    val id = ConferencingUserId.create();
    return conferencingUser()
        .id(id)
        .orgId(TestData.orgId())
        .email(id + "@example.com")
        .name("User " + id)
        .authMethod(AuthMethod.CONF_TEAMS_OAUTH)
        .expireAt(Instant.now().plusSeconds(300))
        .createdAt(Instant.now().minusSeconds(60));
  }
}
