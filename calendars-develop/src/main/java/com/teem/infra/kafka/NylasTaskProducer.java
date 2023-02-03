package com.UoU.infra.kafka;

import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.events.EventExternalId;
import com.UoU.core.events.EventId;
import com.UoU.core.nylas.tasks.NylasTaskScheduler;
import com.UoU.infra.avro.tasks.ChangeCalendar;
import com.UoU.infra.avro.tasks.ChangeCalendarAction;
import com.UoU.infra.avro.tasks.ChangeEvent;
import com.UoU.infra.avro.tasks.ChangeEventAction;
import com.UoU.infra.avro.tasks.DeleteAccountFromNylas;
import com.UoU.infra.avro.tasks.ExportCalendarsToNylas;
import com.UoU.infra.avro.tasks.ImportAllCalendarsFromNylas;
import com.UoU.infra.avro.tasks.SyncAllEvents;
import com.UoU.infra.avro.tasks.UpdateAccountSyncState;
import com.UoU.infra.avro.tasks.UpdateAllSubaccountTokens;
import com.UoU.infra.avro.tasks.UpdateSubaccountToken;
import java.util.Collection;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
class NylasTaskProducer implements NylasTaskScheduler {
  private final Sender sender;
  private final TopicNames.Tasks topicNames;

  @Override
  public void updateAllSubaccountTokens(ServiceAccountId serviceAccountId) {
    sender.send(
        topicNames.getUpdateAllSubaccountTokens(),
        new UpdateAllSubaccountTokens(serviceAccountId.value().toString()));
  }

  @Override
  public void updateSubaccountToken(ServiceAccountId serviceAccountId, AccountId accountId) {
    sender.send(
        topicNames.getUpdateSubaccountToken(),
        new UpdateSubaccountToken(serviceAccountId.value().toString(), accountId.value()));
  }

  @Override
  public void updateAccountSyncState(AccountId accountId) {
    sender.send(
        topicNames.getUpdateAccountSyncState(),
        UpdateAccountSyncState.newBuilder()
            .setAccountId(accountId.value())
            .build());
  }

  @Override
  public void deleteAccountFromNylas(AccountId accountId) {
    sender.send(
        topicNames.getDeleteAccountFromNylas(),
        DeleteAccountFromNylas.newBuilder()
            .setAccountId(accountId.value())
            .build());
  }

  @Override
  public void importAllCalendarsFromNylas(AccountId accountId, boolean includeEvents) {
    sender.send(
        topicNames.getImportAllCalendarsFromNylas(),
        ImportAllCalendarsFromNylas.newBuilder()
            .setAccountId(accountId.value())
            .setIncludeEvents(includeEvents)
            .setInboundSyncAccountLock(UUID.randomUUID().toString())
            .build());
  }

  @Override
  public void importCalendarFromNylas(
      AccountId accountId, CalendarExternalId calendarExternalId, boolean includeEvents) {
    sender.send(
        topicNames.getChangeCalendar(),
        calendarExternalId.value(),
        ChangeCalendar.newBuilder()
            .setAccountId(accountId.value())
            .setCalendarExternalId(calendarExternalId.value())
            .setAction(includeEvents
                ? ChangeCalendarAction.IMPORT_FROM_NYLAS_WITH_EVENTS
                : ChangeCalendarAction.IMPORT_FROM_NYLAS)
            .build());
  }

  @Override
  public void handleCalendarDeleteFromNylas(
      AccountId accountId, CalendarExternalId calendarExternalId) {
    sender.send(
        topicNames.getChangeCalendar(),
        calendarExternalId.value(),
        ChangeCalendar.newBuilder()
            .setAccountId(accountId.value())
            .setCalendarExternalId(calendarExternalId.value())
            .setAction(ChangeCalendarAction.DELETE)
            .build());
  }

  @Override
  public void exportCalendarsToNylas(Collection<CalendarId> ids, boolean includeEvents) {
    if (ids.isEmpty()) {
      throw new IllegalArgumentException("Missing calendar ids");
    }

    val idStrings = ids.stream().map(x -> x.value()).toList();
    val value = ExportCalendarsToNylas.newBuilder()
        .setIds(idStrings)
        .setIncludeEvents(includeEvents)
        .build();

    // If single id, use id as key so multiple tasks for calendar all go to the same partition.
    if (idStrings.size() == 1) {
      sender.send(topicNames.getExportCalendarsToNylas(), idStrings.get(0), value);
    } else {
      sender.send(topicNames.getExportCalendarsToNylas(), value);
    }
  }

  @Override
  public void syncAllEvents(
      AccountId accountId, CalendarId calendarId, boolean forceUpdateAllDayEventWhens,
      UUID inboundSyncAccountLock) {
    sender.send(
        topicNames.getSyncAllEvents(),
        calendarId.value(),
        SyncAllEvents.newBuilder()
            .setAccountId(accountId.value())
            .setCalendarId(calendarId.value())
            .setForceUpdateAllDayEventWhens(forceUpdateAllDayEventWhens)
            .setInboundSyncAccountLock(
                inboundSyncAccountLock == null ? null : inboundSyncAccountLock.toString())
            .build());
  }

  @Override
  public void importEventFromNylas(AccountId accountId, EventExternalId externalId) {
    sender.send(
        topicNames.getChangeEvent(),
        externalId.value(),
        ChangeEvent.newBuilder()
            .setAccountId(accountId.value())
            .setExternalId(externalId.value())
            .setAction(ChangeEventAction.IMPORT_FROM_NYLAS)
            .build());
  }

  @Override
  public void handleEventDeleteFromNylas(AccountId accountId, EventExternalId externalId) {
    sender.send(
        topicNames.getChangeEvent(),
        externalId.value(),
        ChangeEvent.newBuilder()
            .setAccountId(accountId.value())
            .setExternalId(externalId.value())
            .setAction(ChangeEventAction.DELETE)
            .build());
  }

  @Override
  public void exportEventToNylas(AccountId accountId, EventId eventId) {
    sender.send(
        topicNames.getChangeEvent(),
        ChangeEvent.newBuilder()
            .setAccountId(accountId.value())
            .setEventId(eventId.value().toString())
            .setAction(ChangeEventAction.EXPORT_TO_NYLAS)
            .build());
  }

  @Override
  public void deleteEventFromNylas(AccountId accountId, EventExternalId externalId) {
    sender.send(
        topicNames.getChangeEvent(),
        externalId.value(),
        ChangeEvent.newBuilder()
            .setAccountId(accountId.value())
            .setExternalId(externalId.value())
            .setAction(ChangeEventAction.DELETE_FROM_NYLAS)
            .build());
  }
}
