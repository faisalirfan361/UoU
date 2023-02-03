package com.UoU._integration.core.nylas.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nylas.Event;
import com.nylas.EventQuery;
import com.UoU._fakes.nylas.NylasMockFactory;
import com.UoU._helpers.TestData;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.events.Recurrence;
import com.UoU.core.exceptions.ReadOnlyException;
import java.time.Instant;
import java.util.List;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExportEventToNylasTaskTests extends BaseNylasTaskTest {
  AccountId accountId;
  CalendarId calendarId;
  CalendarExternalId calendarExternalId;

  @BeforeEach
  void setup() {
    accountId = dbHelper.createAccount(orgId);
    calendarExternalId = TestData.calendarExternalId();
    calendarId = dbHelper.createCalendar(orgId, accountId, calendarExternalId);
    getEventPublisherMock().reset();
  }

  @SneakyThrows
  @Test
  void createEvent_shouldWork() {
    val eventId = dbHelper.createEvent(orgId, calendarId);
    val nylasEvent = NylasMockFactory.createEventMock(calendarExternalId);
    when(getAccountClientMock().events().create(any(Event.class), any(Boolean.class)))
        .thenReturn(nylasEvent);

    getNylasTaskRunnerSpy().exportEventToNylas(accountId, eventId);
    verify(getAccountClientMock().events()).create(any(Event.class), any(Boolean.class));
    assertThat(dbHelper.getEvent(eventId).getExternalId()).isEqualTo(nylasEvent.getId());
  }

  @Test
  @SneakyThrows
  void createEvent_shouldThrowForReadOnlyEvent() {
    val eventId = dbHelper.createEvent(orgId, calendarId, x -> x.isReadOnly(true));

    assertThatCode(() -> getNylasTaskRunnerSpy().exportEventToNylas(accountId, eventId))
        .isInstanceOf(ReadOnlyException.class);
    verifyNoInteractions(getAccountClientMock());
  }

  @Test
  @SneakyThrows
  void createEvent_shouldUpdateExternalIdWhenFullUpdateFails() {
    val eventId = dbHelper.createEvent(orgId, calendarId);
    val nylasEvent = NylasMockFactory.createEventMock(calendarExternalId);
    when(getAccountClientMock().events().create(any(Event.class), any(Boolean.class)))
        .thenReturn(nylasEvent);

    // Force mapping to throw so full event update never works.
    val mappingException = new RuntimeException("Test mapping error");
    when(getNylasEventMapperSpy().toUpdateRequestModel(any(), any()))
        .thenThrow(mappingException);

    assertThatCode(() -> getNylasTaskRunnerSpy().exportEventToNylas(accountId, eventId))
        .as("Mapping error should cause full update to fail")
        .isEqualTo(mappingException);
    assertThat(dbHelper.getEvent(eventId).getExternalId())
        .as("External id should still be updated after mapping error")
        .isEqualTo(nylasEvent.getId());
  }

  @Test
  @SneakyThrows
  void updateEvent_shouldWork() {
    val externalId = TestData.eventExternalId();
    val eventId = dbHelper.createEvent(orgId, calendarId, externalId);

    val nylasEvent = NylasMockFactory.createEventMock(calendarExternalId);
    when(nylasEvent.getId()).thenReturn(externalId.value());
    when(getAccountClientMock().events().get(externalId.value())).thenReturn(nylasEvent);
    when(getAccountClientMock().events().update(any(Event.class), any(Boolean.class)))
        .thenReturn(nylasEvent);

    // Run export: Nylas update will have changes that should persist back to local db.
    getNylasTaskRunnerSpy().exportEventToNylas(accountId, eventId);
    val localEvent = dbHelper.getEvent(eventId);

    verify(getAccountClientMock().events()).update(any(Event.class), any(Boolean.class));
    assertThat(localEvent)
        .as("Local event should be updated because nylas update resulted in changes.")
        .matches(x -> x.getUpdatedAt() != null)
        .returns(nylasEvent.getTitle(), x -> x.getTitle());

    // Export again: This time, there should be no new nylas changes, so local update should skip.
    getNylasTaskRunnerSpy().exportEventToNylas(accountId, eventId);

    verify(getAccountClientMock().events(), times(2)).update(any(Event.class), any(Boolean.class));
    assertThat(dbHelper.getEvent(eventId).getUpdatedAt())
        .as("Local event should NOT be updated because nylas update did not cause changes.")
        .isEqualTo(localEvent.getUpdatedAt());
  }

  @Test
  @SneakyThrows
  void updateEvent_shouldConvertRecurringInstanceToOverride() {
    // Create local recurrence master and associated non-override instance.
    val masterExternalId = TestData.eventExternalId();
    val masterId = dbHelper.createEvent(orgId, calendarId, x -> x
            .recurrence(TestData.recurrenceMaster())
            .externalId(masterExternalId));
    val instanceExternalId = TestData.eventExternalId();
    val instanceId = dbHelper.createEvent(orgId, calendarId, x -> x
            .recurrence(Recurrence.instance(masterId, false))
            .externalId(instanceExternalId));

    // Create the starting nylas mock, which is a non-override instance.
    // This mock is what the task will start with when fetching the event from nylas.
    val oldNylasInstance = NylasMockFactory.createEventMock(calendarExternalId);
    when(oldNylasInstance.getId()).thenReturn(instanceExternalId.value());
    when(oldNylasInstance.getMasterEventId()).thenReturn("master"); // id doesn't matter
    when(oldNylasInstance.getOriginalStartTime()).thenReturn(null); // non-override
    when(getAccountClientMock().events().list(any(EventQuery.class)).fetchAll())
        .thenReturn(List.of(oldNylasInstance));

    // Update the local event to match the nylas mock, so we start out with local/nylas in sync.
    dbHelper.getEventRepo().update(
        getNylasEventMapperSpy().toUpdateRequestModel(
            oldNylasInstance, dbHelper.getEventRepo().get(instanceId)));

    // Now, create a new mock for the nylas instance where it's changed to an override instance.
    // This mock is what will be returned when the task updates the event in nylas.
    val newNylasInstance = NylasMockFactory.copyEventMock(oldNylasInstance);
    val newInstanceExternalId = TestData.eventExternalId();
    when(newNylasInstance.getId()).thenReturn(newInstanceExternalId.value()); // id is changed
    when(newNylasInstance.getOriginalStartTime()).thenReturn(Instant.now()); // override
    when(getAccountClientMock().events().update(any(Event.class), any(Boolean.class)))
        .thenReturn(newNylasInstance);

    getNylasTaskRunnerSpy().exportEventToNylas(accountId, instanceId);
    val resultInstance = dbHelper.getEventRepo().get(instanceId);

    assertThat(resultInstance.externalId())
        .as("Instance external id was changed to new Nylas value")
        .isEqualTo(newInstanceExternalId);
    assertThat(resultInstance.recurrence().getInstance().masterId())
        .as("Instance masterId should remain unchanged")
        .isEqualTo(masterId);
    assertThat(resultInstance.recurrence().getInstance().isOverride())
        .as("Instance override should have changed to true")
        .isTrue();

    verifyEventPublisherMock()
        .noEventCreated()
        .hasEventUpdated(instanceId)
        .noEventDeleted();
  }
}
