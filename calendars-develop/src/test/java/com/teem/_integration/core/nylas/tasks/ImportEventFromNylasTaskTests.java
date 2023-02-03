package com.UoU._integration.core.nylas.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.nylas.Event;
import com.nylas.EventQuery;
import com.nylas.RequestFailedException;
import com.UoU._fakes.nylas.NylasMockFactory;
import com.UoU._helpers.TestData;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.calendars.CalendarUpdateRequest;
import com.UoU.core.events.DataSource;
import com.UoU.core.events.EventExternalId;
import com.UoU.core.events.EventUpdateRequest;
import com.UoU.core.events.Recurrence;
import com.UoU.core.nylas.ExternalEtag;
import com.UoU.core.nylas.NylasValues;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.SneakyThrows;
import lombok.val;
import org.jooq.exception.NoDataFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ImportEventFromNylasTaskTests extends BaseNylasTaskTest {
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

  @Test
  @SneakyThrows
  void eventCreated_shouldWork() {
    val nylasEvent = NylasMockFactory.createEventMock(calendarExternalId);
    val externalId = new EventExternalId(nylasEvent.getId());

    when(getAccountClientMock().events().get(any(String.class))).thenReturn(nylasEvent);

    getNylasTaskRunnerSpy().importEventFromNylas(accountId, externalId);
    val id = dbHelper.getEventIdByExternalId(externalId);

    validate(nylasEvent, externalId, calendarExternalId);
    verifyEventPublisherMock()
        .hasEventCreated(id)
        .noEventUpdated()
        .noEventDeleted();
  }

  @Test
  @SneakyThrows
  void eventUpdated_shouldWork() {
    val nylasEvent = NylasMockFactory.createEventMock(calendarExternalId);
    val externalId = new EventExternalId(nylasEvent.getId());
    val eventId = dbHelper.createEvent(orgId, calendarId, externalId);

    when(getAccountClientMock().events().get(any(String.class))).thenReturn(nylasEvent);

    getNylasTaskRunnerSpy().importEventFromNylas(accountId, externalId);
    val id = dbHelper.getEventIdByExternalId(externalId);

    validate(nylasEvent, eventId, calendarExternalId);
    verifyEventPublisherMock()
        .noEventCreated()
        .hasEventUpdated(id)
        .noEventDeleted();
  }

  @Test
  @SneakyThrows
  void eventCancelled_shouldDeleteRecurringMasterAndInstances() {
    // Create local recurrence master and instance.
    val masterExternalId = TestData.eventExternalId();
    val masterId = dbHelper.createEvent(orgId, calendarId, x -> x
        .recurrence(TestData.recurrenceMaster())
        .externalId(masterExternalId));
    val instanceExternalId = TestData.eventExternalId();
    val instanceId = dbHelper.createEvent(orgId, calendarId, x -> x
        .recurrence(Recurrence.instance(masterId, false))
        .externalId(instanceExternalId));

    // Setup the nylas master event as cancelled.
    val nylasEvent = NylasMockFactory.createEventMock(calendarExternalId);
    when(nylasEvent.getStatus()).thenReturn(NylasValues.EventStatus.CANCELLED);
    when(getAccountClientMock().events().get(any(String.class))).thenReturn(nylasEvent);

    // Create etags to make sure they get deleted too:
    redisHelper.getExternalEtagRepo().save(masterExternalId, new ExternalEtag("master"));
    redisHelper.getExternalEtagRepo().save(instanceExternalId, new ExternalEtag("instance"));

    getNylasTaskRunnerSpy().importEventFromNylas(accountId, masterExternalId);

    assertThrows(NoDataFoundException.class, () -> dbHelper.getEvent(masterId));
    assertThrows(NoDataFoundException.class, () -> dbHelper.getEvent(instanceId));
    assertThat(redisHelper.getExternalEtagRepo().get(masterExternalId)).isEmpty();
    assertThat(redisHelper.getExternalEtagRepo().get(instanceExternalId)).isEmpty();
    verifyEventPublisherMock()
        .noEventCreated()
        .noEventUpdated()
        .hasEventDeleted(DataSource.PROVIDER, masterId, instanceId);
  }

  @Test
  @SneakyThrows
  void shouldSkipUpdateForMatchingExternalEtag() {
    val nylasEvent = NylasMockFactory.createEventMock(calendarExternalId);
    val externalId = new EventExternalId(nylasEvent.getId());
    val eventId = dbHelper.createEvent(orgId, calendarId, externalId);

    when(getAccountClientMock().events().get(externalId.value())).thenReturn(nylasEvent);

    // Populate external etag for event, then import.
    redisHelper.getExternalEtagRepo().save(externalId, new ExternalEtag(nylasEvent));
    getNylasTaskRunnerSpy().importEventFromNylas(accountId, externalId);

    assertThat(dbHelper.getEvent(eventId).getUpdatedAt())
        .as("Event should not be updated when matching etag exists.")
        .isNull();
    verifyEventPublisherMock()
        .noEventChangedOfAnyType();

    // Now, make external etag NOT match, and make sure update does happen.
    redisHelper.getExternalEtagRepo().save(externalId, new ExternalEtag("will-not-match"));
    getNylasTaskRunnerSpy().importEventFromNylas(accountId, externalId);

    assertThat(dbHelper.getEvent(eventId).getUpdatedAt())
        .as("Event should be updated when no matching etag exists.")
        .isNotNull();
    validate(nylasEvent, eventId, calendarExternalId);
    verifyEventPublisherMock()
        .noEventCreated()
        .hasEventUpdated(eventId)
        .noEventDeleted();
  }

  @Test
  @SneakyThrows
  void after_exportEventToNylas_shouldSkipUpdateForMatchingExternalEtag() {
    val nylasEvent = NylasMockFactory.createEventMock(calendarExternalId);
    val externalId = new EventExternalId(nylasEvent.getId());
    val eventId = dbHelper.createEvent(orgId, calendarId, externalId);

    // Export event: Should create event and save etag.
    when(getAccountClientMock().events().create(any(Event.class), any(Boolean.class)))
        .thenReturn(nylasEvent);
    getNylasTaskRunnerSpy().exportEventToNylas(accountId, eventId);

    assertThat(dbHelper.getEvent(eventId))
        .as("Event should have been updated to save external id.")
        .matches(x -> x.getUpdatedAt() != null && x.getExternalId() != null);
    assertThat(redisHelper.getExternalEtagRepo().get(externalId).toString())
        .as("Export should have created etag.")
        .isNotBlank();

    // Import event: should skip update because of etag. Update the event title manually so we can
    // make sure db row is not touched by the import.
    val title = "title that should not be updated";
    dbHelper.getEventRepo().update(EventUpdateRequest.builder()
        .id(eventId)
        .title(title)
        .build());
    when(getAccountClientMock().events().get(externalId.value())).thenReturn(nylasEvent);
    getNylasTaskRunnerSpy().importEventFromNylas(accountId, externalId);

    assertThat(dbHelper.getEvent(eventId).getTitle())
        .as("Event should not be updated when matching etag exists.")
        .isEqualTo(title);

    // Events should NOT be published because only ExternalId changed which won't matter for event.
    verifyEventPublisherMock()
        .noEventChangedOfAnyType();
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

    // Mock nylas to return the master and list of instances.
    when(getAccountClientMock().events().get(masterExternalId.value())).thenReturn(master);
    when(getAccountClientMock().events().list(any(EventQuery.class)).fetchAll())
        .thenReturn(List.of(instance));

    // Initial import: master and instance should be created locally.
    getNylasTaskRunnerSpy().importEventFromNylas(accountId, masterExternalId);
    val masterId = dbHelper.getEventIdByExternalId(masterExternalId);
    val instanceId = dbHelper.getEventIdByExternalId(instanceExternalId);

    assertThat(dbHelper.getEventByExternalId(masterExternalId))
        .as("Master should be created.")
        .matches(x -> x.getCreatedAt() != null && x.getUpdatedAt() == null);
    assertThat(dbHelper.getEventByExternalId(instanceExternalId))
        .as("Instance should be created.")
        .matches(x -> x.getCreatedAt() != null && x.getUpdatedAt() == null);
    verifyEventPublisherMock()
        .hasEventCreated(masterId, instanceId)
        .noEventUpdated()
        .noEventDeleted()
        .resetMock();

    // Import again without changes: neither master nor instance should be updated (no change).
    getNylasTaskRunnerSpy().importEventFromNylas(accountId, masterExternalId);

    assertThat(dbHelper.getEventByExternalId(masterExternalId).getUpdatedAt())
        .as("Master update should be skipped because there's no change.")
        .isNull();
    assertThat(dbHelper.getEventByExternalId(instanceExternalId).getUpdatedAt())
        .as("Instance update should be skipped because there's no change.")
        .isNull();
    verifyEventPublisherMock()
        .noEventChangedOfAnyType();

    // Change master and import again: Only master should be updated.
    when(master.getTitle()).thenReturn("updated");
    getNylasTaskRunnerSpy().importEventFromNylas(accountId, masterExternalId);
    val masterUpdatedAt = dbHelper.getEventByExternalId(masterExternalId).getUpdatedAt();

    assertThat(masterUpdatedAt)
        .as("Master should be updated because there was a change.")
        .isCloseToUtcNow(within(10, ChronoUnit.SECONDS));
    assertThat(dbHelper.getEventByExternalId(instanceExternalId).getUpdatedAt())
        .as("Instance update should be skipped because there's no change.")
        .isNull();
    verifyEventPublisherMock()
        .noEventCreated()
        .hasEventUpdated(masterId)
        .noEventDeleted()
        .resetMock();

    // Change instance and import again: Only instance should be updated.
    when(instance.getLocation()).thenReturn("updated");
    getNylasTaskRunnerSpy().importEventFromNylas(accountId, masterExternalId);

    assertThat(dbHelper.getEventByExternalId(masterExternalId).getUpdatedAt())
        .as("Master update should be skipped because there's no change.")
        .isEqualTo(masterUpdatedAt);
    assertThat(dbHelper.getEventByExternalId(instanceExternalId).getUpdatedAt())
        .as("Instance should be updated because there was a change.")
        .isCloseToUtcNow(within(10, ChronoUnit.SECONDS));
    verifyEventPublisherMock()
        .noEventCreated()
        .hasEventUpdated(instanceId)
        .noEventDeleted()
        .resetMock();

    // Remove instance from nylas and import again: Instance should be deleted locally.
    when(getAccountClientMock().events().list(any(EventQuery.class)).fetchAll())
        .thenReturn(List.of());
    getNylasTaskRunnerSpy().importEventFromNylas(accountId, masterExternalId);

    assertThat(dbHelper.getEventByExternalId(masterExternalId).getUpdatedAt())
        .as("Master update should be skipped because there's no change.")
        .isEqualTo(masterUpdatedAt);
    assertThat(dbHelper.tryGetEventByExternalId(instanceExternalId))
        .as("Instance should be deleted because it was missing from nylas.")
        .isEmpty();
    verifyEventPublisherMock()
        .noEventCreated()
        .noEventUpdated()
        .hasEventDeleted(DataSource.PROVIDER, instanceId)
        .resetMock();

    // Remove master from nylas and import again: Master should be deleted locally.
    when(getAccountClientMock().events().get(masterExternalId.value()))
        .thenThrow(new RequestFailedException(404, "404", "404"));
    getNylasTaskRunnerSpy().importEventFromNylas(accountId, masterExternalId);

    assertThat(dbHelper.tryGetEventByExternalId(masterExternalId))
        .as("Master should be deleted because it was missing from nylas.")
        .isEmpty();
    verifyEventPublisherMock()
        .noEventCreated()
        .noEventUpdated()
        .hasEventDeleted(DataSource.PROVIDER, masterId)
        .resetMock();
  }

  @Test
  @SneakyThrows
  void shouldCreateRecurringInstanceForExistingMasterOnMasterImport() {
    // Create a recurring master event and one instance event.
    val masterAndInstance = NylasMockFactory.createRecurringMasterAndInstanceMocks(
        calendarExternalId);
    val master = masterAndInstance.getLeft();
    val instance = masterAndInstance.getRight();
    val masterExternalId = new EventExternalId(master.getId());
    val instanceExternalId = new EventExternalId(instance.getId());

    // Locally, create only the master, so import will tie instance to existing master.
    val masterId = dbHelper.createEvent(orgId, calendarId, masterExternalId);

    // Mock nylas to return the master and list of instances.
    when(getAccountClientMock().events().get(masterExternalId.value())).thenReturn(master);
    when(getAccountClientMock().events().list(any(EventQuery.class)).fetchAll())
        .thenReturn(List.of(instance));

    // Import the master:
    getNylasTaskRunnerSpy().importEventFromNylas(accountId, masterExternalId);

    assertThat(dbHelper.getEventByExternalId(masterExternalId))
        .as("Existing master should be updated.")
        .returns(masterId.value(), x -> x.getId())
        .matches(x -> x.getUpdatedAt() != null);
    assertThat(dbHelper.getEventByExternalId(instanceExternalId).getRecurrenceMasterId())
        .as("Instance should be created for master.")
        .isEqualTo(masterId.value());
    verifyEventPublisherMock()
        .hasEventCreated(dbHelper.getEventIdByExternalId(instanceExternalId))
        .hasEventUpdated(masterId)
        .noEventDeleted();
  }

  @Test
  @SneakyThrows
  void shouldCreateRecurringInstanceForExistingMasterOnInstanceImport() {
    // Create a recurring master event and one instance event.
    val masterAndInstance = NylasMockFactory.createRecurringMasterAndInstanceMocks(
        calendarExternalId);
    val master = masterAndInstance.getLeft();
    val instance = masterAndInstance.getRight();
    val masterExternalId = new EventExternalId(master.getId());
    val instanceExternalId = new EventExternalId(instance.getId());

    // Locally, create only the master, so import will tie instance to existing master.
    val masterId = dbHelper.createEvent(orgId, calendarId, masterExternalId);

    // Mock nylas to return the instance.
    when(getAccountClientMock().events().get(instanceExternalId.value())).thenReturn(instance);

    // Import the instance:
    getNylasTaskRunnerSpy().importEventFromNylas(accountId, instanceExternalId);

    assertThat(dbHelper.getEventByExternalId(masterExternalId))
        .as("Master should be the one that already existed.")
        .returns(masterId.value(), x -> x.getId())
        .as("Master should not be updated because we imported the instance.")
        .matches(x -> x.getUpdatedAt() == null);
    assertThat(dbHelper.getEventByExternalId(instanceExternalId).getRecurrenceMasterId())
        .as("Instance should be created for master.")
        .isEqualTo(masterId.value());
    verifyEventPublisherMock()
        .hasEventCreated(dbHelper.getEventIdByExternalId(instanceExternalId))
        .noEventUpdated()
        .noEventDeleted();
  }

  @Test
  @SneakyThrows
  void shouldHandleRecurrenceMasterMissingOnInstanceImport() {
    // Create a recurring instance associated with a non-existent master.
    val instance = NylasMockFactory.createEventMock(calendarExternalId);
    val instanceExternalId = new EventExternalId(instance.getId());
    when(instance.getMasterEventId()).thenReturn(TestData.uuidString());

    // Mock nylas to return the instance.
    when(getAccountClientMock().events().get(instanceExternalId.value())).thenReturn(instance);

    // Import the instance:
    getNylasTaskRunnerSpy().importEventFromNylas(accountId, instanceExternalId);

    assertThat(dbHelper.getEventByExternalId(instanceExternalId).getRecurrenceMasterId())
        .as("Instance should be created as non-recurring because master was missing.")
        .isNull();
    verifyEventPublisherMock()
        .hasEventCreated(dbHelper.getEventIdByExternalId(instanceExternalId))
        .noEventUpdated()
        .noEventDeleted();
  }

  @Test
  @SneakyThrows
  void shouldSkipCreateForReadOnlyCalendar() {
    // Make calendar read-only.
    dbHelper.getCalendarRepo().update(CalendarUpdateRequest.builder()
        .id(calendarId)
        .orgId(orgId)
        .isReadOnly(true)
        .build());

    val event = NylasMockFactory.createEventMock(calendarExternalId);
    val externalId = new EventExternalId(event.getId());

    when(getAccountClientMock().events().get(externalId.value())).thenReturn(event);

    getNylasTaskRunnerSpy().importEventFromNylas(accountId, externalId);

    assertThat(dbHelper.tryGetEventByExternalId(externalId))
        .as("Event should not be imported because calendar is read-only.")
        .isEmpty();
    assertThat(redisHelper.getExternalEtagRepo().get(externalId))
        .as("Etag should be created so next import can be skipped without hitting db.")
        .isPresent();
    verifyEventPublisherMock()
        .noEventChangedOfAnyType();
  }

  @Test
  @SneakyThrows
  void shouldSkipCreateForEventOutsideActivePeriod() {
    val syncPeriod = getEventsConfig().activePeriod();
    val event = NylasMockFactory.createEventMock(calendarExternalId);
    val externalId = new EventExternalId(event.getId());

    when(event.getWhen()).thenReturn(new Event.Date(
        syncPeriod.current().end().atOffset(ZoneOffset.UTC).toLocalDate().plusDays(5)));
    when(getAccountClientMock().events().get(externalId.value())).thenReturn(event);

    getNylasTaskRunnerSpy().importEventFromNylas(accountId, externalId);

    assertThat(dbHelper.tryGetEventByExternalId(externalId))
        .as("Event should not be imported because it's outside sync active period.")
        .isEmpty();
    assertThat(redisHelper.getExternalEtagRepo().get(externalId))
        .as("Etag should be created so next import can be skipped without hitting db.")
        .isPresent();
    verifyEventPublisherMock()
        .noEventChangedOfAnyType();
  }
}
