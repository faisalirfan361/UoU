package com.UoU._integration.core.nylas;

import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.AccountRepository;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.auth.AuthService;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.calendars.CalendarRepository;
import com.UoU.core.calendars.InternalCalendarsConfig;
import com.UoU.core.events.EventExternalId;
import com.UoU.core.events.EventId;
import com.UoU.core.events.EventPublisher;
import com.UoU.core.events.EventRepository;
import com.UoU.core.nylas.ExternalEtagRepository;
import com.UoU.core.nylas.InboundSyncLocker;
import com.UoU.core.nylas.NylasClientFactory;
import com.UoU.core.nylas.auth.NylasAuthService;
import com.UoU.core.nylas.mapping.NylasAccountMapper;
import com.UoU.core.nylas.mapping.NylasCalendarMapper;
import com.UoU.core.nylas.mapping.NylasEventMapper;
import com.UoU.core.nylas.tasks.DeleteAccountFromNylasTask;
import com.UoU.core.nylas.tasks.DeleteEventFromNylasTask;
import com.UoU.core.nylas.tasks.EventHelper;
import com.UoU.core.nylas.tasks.ExportCalendarsToNylasTask;
import com.UoU.core.nylas.tasks.ExportEventToNylasTask;
import com.UoU.core.nylas.tasks.HandleCalendarDeleteFromNylasTask;
import com.UoU.core.nylas.tasks.HandleEventDeleteFromNylasTask;
import com.UoU.core.nylas.tasks.ImportAllCalendarsFromNylasTask;
import com.UoU.core.nylas.tasks.ImportCalendarFromNylasTask;
import com.UoU.core.nylas.tasks.ImportEventFromNylasTask;
import com.UoU.core.nylas.tasks.NylasTaskScheduler;
import com.UoU.core.nylas.tasks.SyncAllEventsTask;
import com.UoU.core.nylas.tasks.UpdateAccountSyncStateTask;
import com.UoU.core.nylas.tasks.UpdateAllSubaccountTokensTask;
import com.UoU.core.nylas.tasks.UpdateSubaccountTokenTask;
import java.time.Duration;
import java.util.Collection;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.val;

/**
 * A Nylas "scheduler" for testing that just executes the tasks directly and bypasses Kafka.
 */
@AllArgsConstructor
public class NylasTaskRunner implements NylasTaskScheduler {
  private final NylasClientFactory clientFactory;
  private final AccountRepository accountRepo;
  private final CalendarRepository calendarRepo;
  private final EventRepository eventRepo;
  private final ExternalEtagRepository etagRepo;
  private final NylasAccountMapper nylasAccountMapper;
  private final NylasCalendarMapper nylasCalendarMapper;
  private final NylasEventMapper nylasEventMapper;
  private final AuthService authService;
  private final NylasAuthService nylasAuthService;
  private final EventHelper eventHelper;
  private final EventPublisher eventPublisher;
  private final InboundSyncLocker inboundSyncLocker;
  private final InternalCalendarsConfig internalCalendarsConfig;

  @Override
  public void updateAllSubaccountTokens(ServiceAccountId serviceAccountId) {
    val params = new UpdateAllSubaccountTokensTask.Params(serviceAccountId);
    val task = new UpdateAllSubaccountTokensTask(this, accountRepo);
    task.run(params);
  }

  @Override
  public void updateSubaccountToken(ServiceAccountId serviceAccountId, AccountId accountId) {
    val params = new UpdateSubaccountTokenTask.Params(serviceAccountId, accountId);
    val task = new UpdateSubaccountTokenTask(authService);
    task.run(params);
  }

  @Override
  public void updateAccountSyncState(AccountId accountId) {
    val params = new UpdateAccountSyncStateTask.Params(accountId);
    val task = new UpdateAccountSyncStateTask(accountRepo, nylasAccountMapper, clientFactory);
    task.run(params);
  }

  @Override
  public void deleteAccountFromNylas(AccountId accountId) {
    val params = new DeleteAccountFromNylasTask.Params(accountId);
    val task = new DeleteAccountFromNylasTask(clientFactory);
    task.run(params);
  }

  @Override
  public void importAllCalendarsFromNylas(AccountId accountId, boolean includeEvents) {
    val params = new ImportAllCalendarsFromNylasTask.Params(
        accountId, includeEvents, UUID.randomUUID());
    val task = new ImportAllCalendarsFromNylasTask(clientFactory, accountRepo, calendarRepo,
        nylasCalendarMapper, this, inboundSyncLocker, Duration.ofMinutes(1));
    task.run(params);
  }

  @Override
  public void importCalendarFromNylas(
      AccountId accountId, CalendarExternalId calendarExternalId, boolean includeEvents) {
    val params = new ImportCalendarFromNylasTask.Params(
        accountId,
        calendarExternalId,
        includeEvents);
    val task = new ImportCalendarFromNylasTask(
        clientFactory, calendarRepo, accountRepo, nylasCalendarMapper, this, inboundSyncLocker);
    task.run(params);
  }

  @Override
  public void handleCalendarDeleteFromNylas(
      AccountId accountId, CalendarExternalId calendarExternalId) {
    val params = new HandleCalendarDeleteFromNylasTask.Params(accountId, calendarExternalId);
    val task = new HandleCalendarDeleteFromNylasTask(calendarRepo, inboundSyncLocker);
    task.run(params);
  }

  @Override
  public void exportCalendarsToNylas(Collection<CalendarId> ids, boolean includeEvents) {
    val params = new ExportCalendarsToNylasTask.Params(ids, includeEvents);
    val task = new ExportCalendarsToNylasTask(clientFactory, accountRepo, calendarRepo, this,
        nylasAuthService, inboundSyncLocker, internalCalendarsConfig);
    task.run(params);
  }

  @Override
  public void syncAllEvents(
      AccountId accountId, CalendarId calendarId, boolean forceUpdateAllDayEventWhens,
      UUID inboundSyncAccountLock) {
    val params = new SyncAllEventsTask.Params(
        accountId, calendarId, forceUpdateAllDayEventWhens, inboundSyncAccountLock);
    val task = new SyncAllEventsTask(
        eventHelper, eventRepo, etagRepo, calendarRepo, nylasEventMapper, eventPublisher,
        inboundSyncLocker);
    task.run(params);
  }

  @Override
  public void importEventFromNylas(AccountId accountId, EventExternalId externalId) {
    val params = new ImportEventFromNylasTask.Params(accountId, externalId);
    val task = new ImportEventFromNylasTask(
        eventHelper, eventRepo, etagRepo, nylasEventMapper, eventPublisher, inboundSyncLocker,
        new HandleEventDeleteFromNylasTask(eventRepo, etagRepo, eventPublisher, inboundSyncLocker));
    task.run(params);
  }

  @Override
  public void handleEventDeleteFromNylas(AccountId accountId, EventExternalId externalId) {
    val params = new HandleEventDeleteFromNylasTask.Params(accountId, externalId);
    val task = new HandleEventDeleteFromNylasTask(
        eventRepo, etagRepo, eventPublisher, inboundSyncLocker);
    task.run(params);
  }

  @Override
  public void exportEventToNylas(AccountId accountId, EventId eventId) {
    val params = new ExportEventToNylasTask.Params(accountId, eventId);
    val task = new ExportEventToNylasTask(
        eventHelper, eventRepo, etagRepo, nylasEventMapper, eventPublisher);
    task.run(params);
  }

  @Override
  public void deleteEventFromNylas(AccountId accountId, EventExternalId externalId) {
    val params = new DeleteEventFromNylasTask.Params(accountId, externalId);
    val task = new DeleteEventFromNylasTask(eventHelper, etagRepo);
    task.run(params);
  }
}
