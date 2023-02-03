package com.UoU.core.nylas.tasks;

import com.UoU.core.Task;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.AccountRepository;
import com.UoU.core.calendars.Calendar;
import com.UoU.core.calendars.CalendarCreateRequest;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.calendars.CalendarRepository;
import com.UoU.core.nylas.InboundSyncLocker;
import com.UoU.core.nylas.NylasClientFactory;
import com.UoU.core.nylas.mapping.NylasCalendarMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Inbound: Batch version of {@link ImportCalendarFromNylasTask} for an entire account.
 *
 * <p>This is a one-way sync with Nylas calendars. Since calendars are only expected to be created
 * or updated via their respective providers, we will only pull data from Nylas and should not have
 * to push any changes from the database to Nylas.
 *
 * <p>This uses {@link com.UoU.core.nylas.InboundSyncLocker} to lock the account and prevent other
 * inbound syncs from occurring, which will help prevent race conditions and unnecessary operations.
 */
@Service
@Slf4j
public class ImportAllCalendarsFromNylasTask implements
    Task<ImportAllCalendarsFromNylasTask.Params> {

  private final NylasClientFactory nylasClientFactory;
  private final AccountRepository accountRepo;
  private final CalendarRepository calendarRepo;
  private final NylasCalendarMapper mapper;
  private final NylasTaskScheduler scheduler;
  private final InboundSyncLocker inboundSyncLocker;
  private final Duration inboundSyncAccountLockTtl;

  public ImportAllCalendarsFromNylasTask(NylasClientFactory nylasClientFactory,
      AccountRepository accountRepo,
      CalendarRepository calendarRepo,
      NylasCalendarMapper mapper,
      NylasTaskScheduler scheduler,
      InboundSyncLocker inboundSyncLocker,

      @NonNull
      @Value("${nylas.tasks.import-all-calendars-from-nylas.inbound-sync-account-lock-ttl}")
      Duration inboundSyncAccountLockTtl) {
    this.nylasClientFactory = nylasClientFactory;
    this.accountRepo = accountRepo;
    this.calendarRepo = calendarRepo;
    this.mapper = mapper;
    this.scheduler = scheduler;
    this.inboundSyncLocker = inboundSyncLocker;
    this.inboundSyncAccountLockTtl = inboundSyncAccountLockTtl;
  }

  public record Params(
      @NonNull AccountId accountId,
      boolean includeEvents,
      @NonNull UUID inboundSyncAccountLock
  ) {
  }

  @SneakyThrows
  @Override
  public void run(Params params) {
    if (!inboundSyncLocker.lockAccount(
        params.accountId(), inboundSyncAccountLockTtl, params.inboundSyncAccountLock())) {
      log.debug("Inbound sync locked for {}. Skipping: Import all calendars",
          params.accountId());
      return;
    }

    // TODO: This assumes all the calendars can fit in memory, which may be problematic for big
    // Google accounts later on. We need to explicitly decide on requirements like how many
    // calendars per org/account we're trying to support so we can make sure we design things
    // like this properly. We should make a list of non-functional requirements and document them
    // to guide us going forward.
    val orgId = accountRepo.getAccessInfo(params.accountId()).orgId();
    val token = accountRepo.getAccessToken(params.accountId());
    val nylas = nylasClientFactory.createAccountClient(token);

    val nylasCalendars = nylas.calendars().list().fetchAll();
    val nylasCalendarsMap = nylasCalendars
        .stream()
        .collect(Collectors.toMap(x -> new CalendarExternalId(x.getId()), x -> x));

    val calendarIdsMap = new HashMap<CalendarExternalId, CalendarId>();
    val localCalendarsMap = new HashMap<CalendarExternalId, Calendar>();
    calendarRepo
        .listByAccount(orgId, params.accountId(), true)
        .filter(x -> x.externalId() != null) // shouldn't exist, but just in case
        .forEach(x -> {
          calendarIdsMap.put(x.externalId(), x.id());
          localCalendarsMap.put(x.externalId(), x);
        });

    // Add calendars from Nylas that are not in our database.
    val calendarsToCreate = new HashMap<>(nylasCalendarsMap);
    calendarsToCreate.keySet().removeAll(localCalendarsMap.keySet());

    if (!calendarsToCreate.isEmpty()) {
      val batchCreate = new ArrayList<CalendarCreateRequest>();
      calendarsToCreate.forEach((externalId, calendar) -> {
        val id = CalendarId.create();
        calendarIdsMap.put(externalId, id);
        batchCreate.add(mapper.toCreateRequestModel(calendar, id, orgId));
      });

      calendarRepo.batchCreate(batchCreate);
    }

    // Update calendars in the database with a matching Nylas calendar.
    val calendarsToUpdate = new HashMap<>(nylasCalendarsMap);
    calendarsToUpdate.keySet().retainAll(localCalendarsMap.keySet());
    val calendarsWithTimezoneChanged = new HashSet<CalendarExternalId>();

    if (!calendarsToUpdate.isEmpty()) {
      // Check if the calendars have changed. We only care about changes to name, readOnly, tz.
      val unchangedCalendars = new HashSet<CalendarExternalId>();
      calendarsToUpdate.forEach((externalId, nylasCalendar) -> {
        val calendar = getOrThrow(localCalendarsMap, externalId);
        if (nylasCalendar.getTimezone() != null
            && !nylasCalendar.getTimezone().equals(calendar.timezone())) {
          calendarsWithTimezoneChanged.add(externalId);
        } else if (Objects.equals(nylasCalendar.getName(), calendar.name())
            && Objects.equals(nylasCalendar.isReadOnly(), calendar.isReadOnly())) {
          unchangedCalendars.add(externalId);
        }
      });

      calendarsToUpdate.keySet().removeAll(unchangedCalendars);

      if (!calendarsToUpdate.isEmpty()) {
        val batchUpdate = calendarsToUpdate.entrySet().stream()
            .map(x -> {
              val calendar = getOrThrow(localCalendarsMap, x.getKey());
              return mapper.toUpdateRequestModel(x.getValue(), calendar);
            })
            .toList();

        calendarRepo.batchUpdate(batchUpdate);
      }
    }

    // Delete calendars from the database that do not have a matching Nylas calendar.
    localCalendarsMap.keySet().removeAll(nylasCalendarsMap.keySet());

    if (!localCalendarsMap.isEmpty()) {
      val calendarsToDelete = localCalendarsMap.values().stream().map(x -> x.id()).toList();
      calendarRepo.batchDelete(calendarsToDelete);
    }

    // Sync events if requested and if not read-only (we don't import events for read-only).
    // Also sync events if calendar timezone changed, so we can update all-day event timestamps.
    val calendarsToSyncEvents = nylasCalendarsMap.entrySet().stream()
        .filter(x -> params.includeEvents() || calendarsWithTimezoneChanged.contains(x.getKey()))
        .filter(x -> !x.getValue().isReadOnly()) // skip read-only since they're mostly ignored
        .map(x -> x.getKey())
        .toList();

    if (!calendarsToSyncEvents.isEmpty()) {
      // If there is more than 1 calendar, re-lock account with count equal to calendarIds, and then
      // each syncAllEvents task will decrement the lock when the task finishes, until it reaches 0
      // and the lock is removed. If only 1 calendar, we can just leave existing lock in place.
      if (calendarsToSyncEvents.size() > 1) {
        inboundSyncLocker.lockAccount(
            params.accountId(),
            inboundSyncAccountLockTtl,
            params.inboundSyncAccountLock(),
            calendarsToSyncEvents.size());
      }

      calendarsToSyncEvents.forEach(externalId -> {
        val id = getOrThrow(calendarIdsMap, externalId);
        val forceUpdateAllDayEventWhens = calendarsWithTimezoneChanged.contains(externalId);
        scheduler.syncAllEvents(
            params.accountId(),
            id,
            forceUpdateAllDayEventWhens,
            params.inboundSyncAccountLock());
      });
    } else {
      // There are no calendars to sync events for, so remove lock now.
      inboundSyncLocker.unlockAccount(params.accountId(), params.inboundSyncAccountLock());
    }

    log.debug("Imported all calendars for {}: create={}, update={}",
        params.accountId(), calendarsToCreate.size(), calendarsToUpdate.size());
  }

  /**
   * Helper to get a non-null value from a map, else throw an exception because null is invalid.
   */
  private static <K, V> V getOrThrow(Map<K, V> map, K key) {
    val result = map.get(key);
    if (result == null) {
      throw new NullPointerException("Map value was unexpectedly missing or null");
    }
    return result;
  }
}
