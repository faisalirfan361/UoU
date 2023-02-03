package com.UoU._fakes.nylas;

import static com.UoU._helpers.TestData.calendarExternalId;
import static com.UoU._helpers.TestData.email;
import static com.UoU._helpers.TestData.eventExternalId;
import static com.UoU._helpers.TestData.uuidString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nylas.Account;
import com.nylas.Accounts;
import com.nylas.Calendar;
import com.nylas.Calendars;
import com.nylas.Event;
import com.nylas.Events;
import com.nylas.NylasAccount;
import com.nylas.NylasApplication;
import com.nylas.Participant;
import com.nylas.RemoteCollection;
import com.UoU._helpers.TestData;
import com.UoU.core.Fluent;
import com.UoU.core.SecretString;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.nylas.NylasClientFactory;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

public class NylasMockFactory {
  private static final String TIMEZONE = "America/Chicago";

  public static Event createEventMock(CalendarExternalId calendarExternalId) {
    val event = mock(Event.class);
    val id = eventExternalId().value();
    when(event.hasId()).thenReturn(true);
    when(event.getId()).thenReturn(id);
    when(event.getIcalUid()).thenReturn(uuidString());
    when(event.getCalendarId()).thenReturn(calendarExternalId.value());
    when(event.getTitle()).thenReturn("Nylas Event Title " + id);
    when(event.getDescription()).thenReturn("Nylas Event Description " + id);
    when(event.getLocation()).thenReturn("Nylas Event Location" + id);
    when(event.getWhen()).thenReturn(createTimespan());
    when(event.getStatus()).thenReturn("confirmed");
    when(event.getReadOnly()).thenReturn(false);
    when(event.getOwner()).thenReturn("Owner " + id + "<" + email() + ">");

    val participants = createParticipantMocks().limit(2).toList();
    when(event.getParticipants()).thenReturn(participants);

    when(event.getMetadata()).thenReturn(
        Map.of("checkinAt", Instant.now().toString(), "checkoutAt", ""));

    return event;
  }

  public static Event copyEventMock(Event other) {
    val event = mock(Event.class);

    when(event.hasId()).then(x -> other.hasId());
    when(event.getId()).then(x -> other.getId());
    when(event.getIcalUid()).then(x -> other.getIcalUid());
    when(event.getCalendarId()).then(x -> other.getCalendarId());
    when(event.getTitle()).then(x -> other.getTitle());
    when(event.getDescription()).then(x -> other.getDescription());
    when(event.getLocation()).then(x -> other.getLocation());
    when(event.getWhen()).then(x -> other.getWhen());
    when(event.getRecurrence()).then(x -> other.getRecurrence());
    when(event.getMasterEventId()).then(x -> other.getMasterEventId());
    when(event.getOriginalStartTime()).then(x -> other.getOriginalStartTime());
    when(event.getStatus()).then(x -> other.getStatus());
    when(event.getReadOnly()).then(x -> other.getReadOnly());
    when(event.getOwner()).then(x -> other.getOwner());
    when(event.getParticipants()).then(x -> other.getParticipants());
    when(event.getMetadata()).then(x -> other.getMetadata());

    return event;
  }

  public static Participant createParticipantMock() {
    val participant = mock(com.nylas.Participant.class);
    when(participant.getName()).thenReturn("Test Participant");
    when(participant.getEmail()).thenReturn(email());
    when(participant.getComment()).thenReturn("Comments");
    when(participant.getStatus()).thenReturn("noreply");

    return participant;
  }

  public static Stream<Event> createEventMocks(CalendarExternalId calendarExternalId) {
    return Stream.generate(() -> createEventMock(calendarExternalId));
  }

  public static Stream<Participant> createParticipantMocks() {
    return Stream.generate(() -> createParticipantMock());
  }

  public static Pair<Event, Event> createRecurringMasterAndInstanceMocks(
      CalendarExternalId calendarExternalId) {

    val master = createEventMock(calendarExternalId);
    val masterId = master.getId();
    val instance = createEventMock(calendarExternalId);

    when(master.getRecurrence()).thenReturn(Fluent
        .of(TestData.recurrenceMaster().getMaster())
        .map(x -> new Event.Recurrence(x.timezone(), x.rrule()))
        .get());

    when(instance.getMasterEventId()).thenReturn(masterId);

    return Pair.of(master, instance);
  }

  public static Stream<Calendar> createCalendarMocks(AccountId accountId) {
    return Stream.generate(() -> createCalendarMock(calendarExternalId(), accountId));
  }

  public static Stream<Calendar> createCalendarMocks(
      AccountId accountId, List<CalendarExternalId> calendarIds) {
    return calendarIds.stream().map(x -> createCalendarMock(x, accountId));
  }

  public static Calendar createCalendarMock(
      CalendarExternalId calendarExternalId, AccountId accountId) {
    val calendar = mock(Calendar.class);
    when(calendar.getId()).thenReturn(calendarExternalId.value());
    when(calendar.getAccountId()).thenReturn(accountId.value());
    when(calendar.hasId()).thenReturn(true);
    when(calendar.getName()).thenReturn("Nylas Calendar Name");
    when(calendar.isReadOnly()).thenReturn(false);
    when(calendar.getTimezone()).thenReturn(TIMEZONE);

    return calendar;
  }

  public static Account createAccountMock(AccountId accountId, String syncState) {
    val account = mock(Account.class);
    when(account.getAccountId()).thenReturn(accountId.value());
    when(account.getEmail()).thenReturn(email());
    when(account.getProvider()).thenReturn("ews");
    when(account.getSyncState()).thenReturn(syncState);
    return account;
  }

  public static Event.Timespan createTimespan() {
    return new Event.Timespan(
        ZonedDateTime.ofInstant(Instant.now(), ZoneId.of(TIMEZONE)),
        ZonedDateTime.ofInstant(Instant.now().plus(1, ChronoUnit.HOURS), ZoneId.of(TIMEZONE)));
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  public static NylasAccount createAccountClientMock() {
    val calendars = mock(Calendars.class);
    val calendarRemoteCollection = mock(RemoteCollection.class);
    when(calendars.list()).thenReturn(calendarRemoteCollection);

    val events = mock(Events.class);
    when(events.delete(any(String.class), any(Boolean.class))).thenReturn("jobStatusId");
    val eventRemoteCollection = mock(RemoteCollection.class);
    when(events.list(any())).thenReturn(eventRemoteCollection);
    when(events.list()).thenReturn(eventRemoteCollection);

    val accountClient = mock(NylasAccount.class);
    when(accountClient.events()).thenReturn(events);
    when(accountClient.calendars()).thenReturn(calendars);

    return accountClient;
  }

  @SneakyThrows
  public static NylasApplication createApplicationClient() {
    val accounts = mock(Accounts.class);
    when(accounts.delete(any(String.class))).thenReturn(null);

    val appClient = mock(NylasApplication.class);
    when(appClient.accounts()).thenReturn(accounts);

    return appClient;
  }

  public static NylasClientFactory createClientFactoryMock(NylasAccount accountClient,
                                                           NylasApplication appClient) {

    val clientFactory = mock(NylasClientFactory.class);
    when(clientFactory.createApplicationClient()).thenReturn(appClient);
    when(clientFactory.createAccountClient(any(SecretString.class))).thenReturn(accountClient);

    return clientFactory;
  }
}
