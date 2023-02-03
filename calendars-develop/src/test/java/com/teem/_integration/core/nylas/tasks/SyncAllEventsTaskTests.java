package com.UoU._integration.core.nylas.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nylas.Event;
import com.nylas.EventQuery;
import com.UoU._fakes.nylas.FakeInboundSyncLocker;
import com.UoU._fakes.nylas.NylasMockFactory;
import com.UoU._helpers.TestData;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.calendars.CalendarUpdateRequest;
import com.UoU.core.events.EventExternalId;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.SneakyThrows;
import lombok.val;
import org.jooq.exception.NoDataFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SyncAllEventsTaskTests extends BaseNylasTaskTest {

  private AccountId accountId;
  private CalendarId calendarId;
  private CalendarExternalId calendarExternalId;

  @BeforeEach
  void setUp() {
    accountId = dbHelper.createAccount(orgId);
    calendarExternalId = TestData.calendarExternalId();
    calendarId = dbHelper.createCalendar(orgId, accountId, calendarExternalId);
  }

  void runTask() {
    getNylasTaskRunnerSpy().syncAllEvents(accountId, calendarId);
  }

  @Test
  void shouldAbortIfInboundSyncIsLockedForAccount() {
    FakeInboundSyncLocker.fakeIsAccountLockedResult(accountId, true);
    getNylasTaskRunnerSpy().syncAllEvents(accountId, calendarId);
    getNylasTaskRunnerSpy().syncAllEvents(accountId, calendarId, true, UUID.randomUUID());
    verifyNoInteractions(getAccountClientMock());
  }

  @SneakyThrows
  @Test
  void shouldSyncSeveralEventsInDifferentStates() {
    // nylasEvent1 - doesn't match a local event and should be created in the DB
    // nylasEvent2 - matches a local event (eventId2) and the event in the DB should be updated
    // nylasEvent3 - returned as the created event for eventId3
    val nylasEvent1 = NylasMockFactory.createEventMock(calendarExternalId);
    val nylasEvent2 = NylasMockFactory.createEventMock(calendarExternalId);
    val nylasEvent3 = NylasMockFactory.createEventMock(calendarExternalId);

    // eventId1 - doesn't have a matching Nylas event and should be deleted from the DB
    // eventId2 - matches a Nylas event and the event in the DB should be updated
    // eventId3 = doesn't have a matching Nylas event and should be created in Nylas
    val eventId1 = dbHelper.createEvent(orgId, calendarId, TestData.eventExternalId());
    val eventId2 = dbHelper.createEvent(orgId, calendarId,
        new EventExternalId(nylasEvent2.getId()));
    val eventId3 = dbHelper.createEvent(orgId, calendarId);

    when(getAccountClientMock().events().list(any(EventQuery.class)).fetchAll())
        .thenReturn(List.of(nylasEvent1, nylasEvent2));
    when(getAccountClientMock().events().create(any(Event.class), any(Boolean.class)))
        .thenReturn(nylasEvent3);

    runTask();

    validate(nylasEvent1, new EventExternalId(nylasEvent1.getId()), calendarExternalId);
    validate(nylasEvent2, eventId2, calendarExternalId);

    assertThrows(NoDataFoundException.class, () -> dbHelper.getEvent(eventId1));
    verify(getAccountClientMock().events()).create(any(Event.class), any(Boolean.class));
    assertThat(nylasEvent3.getId()).isEqualTo(dbHelper.getEvent(eventId3).getExternalId());

    val externalIds = Set.of(
        new EventExternalId(nylasEvent1.getId()),
        new EventExternalId(nylasEvent2.getId()),
        new EventExternalId(nylasEvent3.getId()));
    assertThat(redisHelper.getExternalEtagRepo().get(externalIds).keySet())
        .as("Etags should have been saved.")
        .containsExactlyInAnyOrderElementsOf(externalIds);
  }

  @Test
  @SneakyThrows
  void shouldCreateUpdateAndDeleteSingleEvent() {
    val nylasEvent = NylasMockFactory.createEventMock(calendarExternalId);
    val externalId = new EventExternalId(nylasEvent.getId());

    when(getAccountClientMock().events().list(any(EventQuery.class)).fetchAll())
        .thenReturn(List.of(nylasEvent));

    // Sync events: Local event will be created, but not updated yet.
    runTask();
    assertThat(dbHelper.getEventByExternalId(externalId))
        .matches(x -> x.getCreatedAt() != null && x.getUpdatedAt() == null);
    assertThat(redisHelper.getExternalEtagRepo().get(externalId)).isPresent();

    // Sync events again: Local event should NOT be updated because there were no changes.
    runTask();
    assertThat(dbHelper.getEventByExternalId(externalId).getUpdatedAt()).isNull();
    assertThat(redisHelper.getExternalEtagRepo().get(externalId)).isPresent();

    // Change event, and sync again: Local event should now be updated.
    when(nylasEvent.getTitle()).thenReturn("changed");
    runTask();
    assertThat(dbHelper.getEventByExternalId(externalId).getUpdatedAt()).isNotNull();
    assertThat(redisHelper.getExternalEtagRepo().get(externalId)).isPresent();

    // Delete event from nylas, sync again: Local event and etag should be removed.
    when(getAccountClientMock().events().list(any(EventQuery.class)).fetchAll())
        .thenReturn(List.of());
    runTask();
    assertThat(dbHelper.tryGetEventByExternalId(externalId)).isEmpty();
    assertThat(redisHelper.getExternalEtagRepo().get(externalId)).isEmpty();
  }

  @Test
  @SneakyThrows
  void shouldCreateUpdateDeleteRecurringEvents() {
    // Create a recurring master event and one instance event.
    val masterAndInstance = NylasMockFactory.createRecurringMasterAndInstanceMocks(
        calendarExternalId);
    val master = masterAndInstance.getLeft();
    val instance = masterAndInstance.getRight();
    val masterExternalId = new EventExternalId(master.getId());
    val instanceExternalId = new EventExternalId(instance.getId());

    // Mock nylas to return the master and instance.
    when(getAccountClientMock().events().list(any(EventQuery.class)).fetchAll())
        .thenReturn(List.of(master, instance));

    // Initial sync: master and instance should be created locally.
    runTask();

    assertThat(dbHelper.getEventByExternalId(masterExternalId))
        .as("Master should be created.")
        .matches(x -> x.getCreatedAt() != null && x.getUpdatedAt() == null);
    assertThat(dbHelper.getEventByExternalId(instanceExternalId))
        .as("Instance should be created.")
        .matches(x -> x.getCreatedAt() != null && x.getUpdatedAt() == null);

    // Sync again without changes: neither master nor instance should be updated (no change).
    runTask();

    assertThat(dbHelper.getEventByExternalId(masterExternalId).getUpdatedAt())
        .as("Master update should be skipped because there's no change.")
        .isNull();
    assertThat(dbHelper.getEventByExternalId(instanceExternalId).getUpdatedAt())
        .as("Instance update should be skipped because there's no change.")
        .isNull();

    // Change master and sync again: Only master should be updated.
    when(master.getTitle()).thenReturn("updated");
    runTask();
    val masterUpdatedAt = dbHelper.getEventByExternalId(masterExternalId).getUpdatedAt();

    assertThat(masterUpdatedAt)
        .as("Master should be updated because there was a change.")
        .isCloseToUtcNow(within(10, ChronoUnit.SECONDS));
    assertThat(dbHelper.getEventByExternalId(instanceExternalId).getUpdatedAt())
        .as("Instance update should be skipped because there's no change.")
        .isNull();

    // Change instance and sync again: Only instance should be updated.
    when(instance.getLocation()).thenReturn("updated");
    runTask();

    assertThat(dbHelper.getEventByExternalId(masterExternalId).getUpdatedAt())
        .as("Master update should be skipped because there's no change.")
        .isEqualTo(masterUpdatedAt);
    assertThat(dbHelper.getEventByExternalId(instanceExternalId).getUpdatedAt())
        .as("Instance should be updated because there was a change.")
        .isCloseToUtcNow(within(10, ChronoUnit.SECONDS));

    // Remove instance from nylas and sync again: Instance should be deleted locally.
    when(getAccountClientMock().events().list(any(EventQuery.class)).fetchAll())
        .thenReturn(List.of(master));
    runTask();

    assertThat(dbHelper.getEventByExternalId(masterExternalId).getUpdatedAt())
        .as("Master update should be skipped because there's no change.")
        .isEqualTo(masterUpdatedAt);
    assertThat(dbHelper.tryGetEventByExternalId(instanceExternalId))
        .as("Instance should be deleted because it was missing from nylas.")
        .isEmpty();

    // Remove master from nylas and sync again: Master should be deleted locally.
    when(getAccountClientMock().events().list(any(EventQuery.class)).fetchAll())
        .thenReturn(List.of());
    runTask();

    assertThat(dbHelper.tryGetEventByExternalId(masterExternalId))
        .as("Master should be deleted because it was missing from nylas.")
        .isEmpty();
  }

  @SneakyThrows
  @Test
  void shouldUpdateExternalIdWhenFullUpdateFails() {
    val eventId = dbHelper.createEvent(orgId, calendarId);
    val nylasEvent = NylasMockFactory.createEventMock(calendarExternalId);
    when(getAccountClientMock().events().create(any(Event.class), any(Boolean.class)))
        .thenReturn(nylasEvent);

    // Force mapping to throw so full event update never works.
    val mappingException = new RuntimeException("Test mapping error");
    when(getNylasEventMapperSpy().toUpdateRequestModel(any(), any()))
        .thenThrow(mappingException);

    assertThatCode(this::runTask)
        .as("Mapping error should cause full update to fail")
        .isEqualTo(mappingException);
    assertThat(dbHelper.getEvent(eventId).getExternalId())
        .as("External id should still be updated after mapping error")
        .isEqualTo(nylasEvent.getId());
  }

  @Test
  @SneakyThrows
  void shouldForceUpdateAllDayEventWhens() {
    // Mock nylas all-day event.
    val date = LocalDate.now();
    val nylasEvent = NylasMockFactory.createEventMock(calendarExternalId);
    when(nylasEvent.getWhen()).thenReturn(new Event.Date(date));
    val externalId = new EventExternalId(nylasEvent.getId());
    when(getAccountClientMock().events().list(any(EventQuery.class)).fetchAll())
        .thenReturn(List.of(nylasEvent));

    // Start with calendar in a known timezone, which we'll change later.
    val oldTimezone = ZoneId.of("UTC");
    val newTimezone = ZoneId.of("America/Denver");
    updateCalendarTimezone(oldTimezone.getId());

    // Run task once so event gets created.
    runTask();
    val originalEvent = dbHelper.getEventByExternalId(externalId);

    // Change calendar timezone, and run task again with forceUpdateAllDayEventWhens, which should
    // cause the all-day event start and end to update because of the timezone change.
    updateCalendarTimezone(newTimezone.getId());
    getNylasTaskRunnerSpy().syncAllEvents(accountId, calendarId, true);
    val updatedEvent = dbHelper.getEventByExternalId(externalId);

    // Ensure that event start and end timestamps are correct for old and new timezones.
    assertThat(originalEvent.getAllDayStartAt()).isEqualTo(date);
    assertThat(originalEvent.getAllDayEndAt()).isEqualTo(date);
    assertThat(originalEvent.getStartAt())
        .isEqualTo(date.atStartOfDay(oldTimezone).toOffsetDateTime());
    assertThat(originalEvent.getEndAt())
        .isEqualTo(date.plusDays(1).atStartOfDay(oldTimezone).toOffsetDateTime());

    assertThat(updatedEvent.getAllDayStartAt()).isEqualTo(date);
    assertThat(updatedEvent.getAllDayEndAt()).isEqualTo(date);
    assertThat(updatedEvent.getStartAt())
        .isEqualTo(date.atStartOfDay(newTimezone).toOffsetDateTime());
    assertThat(updatedEvent.getEndAt())
        .isEqualTo(date.plusDays(1).atStartOfDay(newTimezone).toOffsetDateTime());
  }

  private void updateCalendarTimezone(String timezone) {
    dbHelper.getCalendarRepo().update(CalendarUpdateRequest.builder()
        .id(calendarId)
        .orgId(orgId)
        .timezone(timezone)
        .build());
  }
}
