package com.UoU.core.nylas.tasks;

import com.nylas.Event;
import com.nylas.NylasAccount;
import com.nylas.RequestFailedException;
import com.UoU.core.Task;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.calendars.CalendarRepository;
import com.UoU.core.events.DataSource;
import com.UoU.core.events.EventCreateRequest;
import com.UoU.core.events.EventExternalId;
import com.UoU.core.events.EventId;
import com.UoU.core.events.EventPublisher;
import com.UoU.core.events.EventRepository;
import com.UoU.core.events.EventUpdateRequest;
import com.UoU.core.nylas.ExternalEtag;
import com.UoU.core.nylas.ExternalEtagRepository;
import com.UoU.core.nylas.InboundSyncLocker;
import com.UoU.core.nylas.NylasValues;
import com.UoU.core.nylas.mapping.NylasEventMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Inbound/Outbound: Does a 2-way sync of all events on the calendar.
 *
 * <p>This uses {@link com.UoU.core.nylas.InboundSyncLocker} to skip sync when another major
 * inbound sync is occurring, which will help prevent race conditions and unnecessary operations.
 * When an inboundSyncAccountLock is provided, this operation is part of a parent operation
 * that has already locked the account. In this case, the task will decrement the lock so that the
 * account will become unlocked once all child operations are complete.
 */
@Service
@AllArgsConstructor
@Slf4j
public class SyncAllEventsTask implements Task<SyncAllEventsTask.Params> {
  private final EventHelper eventHelper;
  private final EventRepository eventRepo;
  private final ExternalEtagRepository etagRepo;
  private final CalendarRepository calendarRepo;
  private final NylasEventMapper mapper;
  private final EventPublisher eventPublisher;
  private final InboundSyncLocker inboundSyncLocker;

  public record Params(
      @NonNull AccountId accountId,
      @NonNull CalendarId calendarId,
      boolean forceUpdateAllDayEventWhens,
      UUID inboundSyncAccountLock
  ) {
  }

  @SneakyThrows
  @Override
  public void run(Params params) {
    // We don't lock the account inbound sync for this operation, but if we are passed a sync lock
    // it means this operation is a child operation and the parent operation has obtained the lock.
    // Therefore, we check if we should proceed based on the passed lock, and unlock it at the end.
    if (inboundSyncLocker.isAccountLocked(params.accountId(), params.inboundSyncAccountLock())) {
      log.debug("Inbound sync locked for {}. Skipping: Sync events for calendar {}",
          params.accountId(), params.calendarId());
      return;
    }

    // TODO: We need error handling in this method and to finish the sync implementation.
    // TODO: This assumes all the events for a calendar can fit in memory at once. We may need to
    // think about or document the scale we're expecting. Also many maps/lists in here get iterated
    // more than is necessary. Once we decide on some of the bigger optimizations like batching,
    // caching, etc., revisit and optimize if still needed.

    val calendar = calendarRepo.get(params.calendarId());

    // Sync will only work for eligible calendars, so if this task is run for a read-only calendar,
    // for example, it's either a bug or someone running the task manually and improperly.
    calendar.requireIsEligibleToSync();

    // Get the events from Nylas for the calendar, including recurring event instances.
    val client = eventHelper.createNylasClient(params.accountId());
    val nylasEventMap = getNylasEvents(client, calendar.externalId());

    // Get the events from the database for the calendar.
    val orgId = calendar.orgId();
    val localEvents = eventRepo.listByCalendar(orgId, params.calendarId()).toList();
    val localEventMap = localEvents.stream()
        .filter(x -> x.externalId() != null)
        .collect(Collectors.toMap(x -> x.externalId().value(), x -> x));

    // Keep list of external etags to save to cache at the end. Note that these are not used to
    // optimize the full sync because we have to fetch all the nylas and local events anyway, but we
    // still persist the updated tags to optimize other operations later.
    val newExternalEtags = new HashMap<EventExternalId, ExternalEtag>();

    // For Nylas events that are found in the database, update them.
    val updateBatch = new ArrayList<EventUpdateRequest>();
    val updateIdsWithChanges = new ArrayList<EventId>();

    nylasEventMap.values().stream()
        .filter(event -> localEventMap.containsKey(event.getId()))
        .forEach(event -> {
          val localEvent = localEventMap.get(event.getId());
          var updateRequest = mapper.toUpdateRequestModel(event, localEvent);

          // When requested, force update all-day event whens, which will also update the calculated
          // all-day timestamps. This is useful when the calendar timezone changes because nothing
          // about the actual event will change in that case except how we interpret all-day times.
          if (params.forceUpdateAllDayEventWhens()
              && updateRequest.when().isAllDay()
              && !updateRequest.hasUpdate(EventUpdateRequest.UpdateField.WHEN)) {
            updateRequest = updateRequest.toBuilder().when(updateRequest.when()).build();
          }

          updateBatch.add(updateRequest);
          newExternalEtags.put(localEvent.externalId(), new ExternalEtag(event));
          if (updateRequest.hasUpdates()) {
            updateIdsWithChanges.add(localEvent.id());
          }
        });

    eventRepo.batchUpdate(updateBatch);
    eventPublisher.eventUpdated(updateIdsWithChanges);

    // For Nylas events where a matching copy isn't found in the database, create them.
    // Create the new local ids first so that they can be referenced as master event ids if needed.
    // Master event ids are used to tie recurring instances back to the recurring master event.
    val newLocalIds = nylasEventMap.keySet().stream()
        .filter(x -> !localEventMap.containsKey(x))
        .collect(Collectors.toMap(x -> x, x -> EventId.create()));
    val createBatch = new ArrayList<EventCreateRequest>();
    newLocalIds.entrySet().stream()
        .forEach(entry -> {
          val nylasId = entry.getKey();
          val event = nylasEventMap.get(nylasId);
          val localId = entry.getValue();
          val masterLocalId = Optional
              .ofNullable(event.getMasterEventId())
              .map(masterNylasId -> Optional
                  .ofNullable(localEventMap.get(masterNylasId))
                  .map(x -> x.id())
                  .or(() -> Optional.ofNullable(newLocalIds.get(masterNylasId)))
                  .orElseGet(() -> {
                    // If the master event is not found, something is really wrong. Rather than
                    // fail the whole sync, import the event without the master id so it will be
                    // treated as non-recurring. But log the error so we can see if this ever
                    // happens, and then we'll possibly need a way to resolve it.
                    log.error(
                        "Master event {} not found for instance {}. Importing as non-recurring.",
                        masterNylasId, nylasId);
                    return null;
                  }));

          createBatch.add(masterLocalId
              .map(masterId -> mapper.toCreateRequestModel(
                  event, localId, masterId, params.calendarId(), orgId))
              .orElseGet(() -> mapper.toCreateRequestModel(
                  event, localId, params.calendarId(), orgId)));

          newExternalEtags.put(new EventExternalId(event.getId()), new ExternalEtag(event));
        });

    eventRepo.batchCreate(createBatch);
    eventPublisher.eventCreated(newLocalIds.values());

    // For events in the db where the external id is missing, create them in Nylas, then update db.
    // We can skip eventPublisher.eventUpdate() because only the externalId should be updated, which
    // is not part of the EventChanged contract, and also the provider will update the icaluid (at
    // least) and cause a webhook update momentarily anyway.
    final long exportSuccessCount = localEvents.stream()
        .filter(x -> x.externalId() == null)
        .map(localEvent -> {
          Event createdEvent;
          EventExternalId externalId;

          try {
            createdEvent = client.events().create(
                mapper.toNylasEvent(localEvent, calendar.externalId()), true);
          } catch (IOException | RequestFailedException ex) {
            log.error("Error while exporting event {} for calendar {}: {}",
                localEvent.id(), params.calendarId, ex.getMessage(), ex);
            return false; // failure
          }

          externalId = new EventExternalId(createdEvent.getId());

          try {
            eventRepo.update(mapper.toUpdateRequestModel(createdEvent, localEvent));
          } catch (Exception ex) {
            // The nylas event has been created, but our update failed. This is a bad state to be in
            // because the externalId will not be set locally, so retries at this point would create
            // duplicate events in nylas. We will try once more to set only externalId to tie the
            // local and nylas events together. If that works, we'll retry the original update.
            log.error("Error while updating exported event {}, {} for calendar {}",
                localEvent.id(), externalId, params.calendarId(), ex);
            eventRepo.updateExternalId(localEvent.id(), externalId, DataSource.PROVIDER);
            eventRepo.update(mapper.toUpdateRequestModel(createdEvent, localEvent));
          }

          newExternalEtags.put(externalId, new ExternalEtag(createdEvent));

          return true; // success
        })
        .filter(x -> x)
        .count();

    // For events in the database where the external id isn't found in Nylas, delete them.
    val deleteBatch = new ArrayList<EventId>();
    val deleteBatchExternalIds = new HashSet<EventExternalId>();
    localEventMap.values().stream()
        .filter(x -> Optional
            .ofNullable(x.externalId())
            .filter(y -> !nylasEventMap.containsKey(y.value()))
            .isPresent())
        .forEach(x -> {
          deleteBatch.add(x.id());
          deleteBatchExternalIds.add(x.externalId());
        });

    eventRepo.batchDelete(deleteBatch);
    eventPublisher.eventDeleted(orgId, params.calendarId(), deleteBatch, DataSource.PROVIDER);

    // Persist external etag changes.
    etagRepo.save(newExternalEtags);
    etagRepo.tryDelete(deleteBatchExternalIds);

    Optional
        .ofNullable(params.inboundSyncAccountLock())
        .ifPresent(lock -> inboundSyncLocker.unlockAccount(params.accountId(), lock));

    log.debug(
        "Synced all events for {}: create={}, update={}, delete={}, exports={}, etags=+{}/-{}",
        params.calendarId(), createBatch.size(), updateBatch.size(), deleteBatch.size(),
        exportSuccessCount, newExternalEtags.size(), deleteBatchExternalIds.size());
  }

  /**
   * Gets all Nylas events within our sync timespan, including expanded recurrence instances.
   *
   * <p>Recurrence masters outside the sync timespan will also be included if they are active for
   * the sync timespan, which means there is at least one instance within the timespan.
   */
  @SneakyThrows
  private Map<String, Event> getNylasEvents(
      NylasAccount client, CalendarExternalId calendarExternalId) {

    val events = new HashMap<String, Event>();
    val masterEventIds = new HashSet<String>();

    // Fetch all recurrence masters in active period, including masters 
    // with no instances.
    // The edge case (master with no instances) is not handled when only fetching 
    // the instances and working backward to then fetch the masters,
    // so this extra fetch is necessary to handle the edge case.
    // EDGE CASE : Master event is created with the RRULE date until
    // the past date when event actually occurring.
    // Ex. Event Date: Jan 20  & RRULE :"FREQ=DAILY;UNTIL=20230119"
    client.events()
        .list(new EventQueryBuilder()
            .calendarExternalId(calendarExternalId)
            .startsWithin(eventHelper.getCurrentActivePeriod())
            .and(x -> x.expandRecurring(false))
            .build())
        .fetchAll()
        .forEach(event -> {
          if (event.getMasterEventId() == null && event.getRecurrence() != null) {
            events.put(event.getId(), event);
          }
        });

    // Fetch normal events and recurrence instances within sync timespan.
    client.events()
        .list(new EventQueryBuilder()
            .calendarExternalId(calendarExternalId)
            .startsWithin(eventHelper.getCurrentActivePeriod())
            .and(x -> x.expandRecurring(true))
            .build())
        .fetchAll()
        .forEach(event -> {
          // Nylas always returns cancelled recurrence instances regardless of query, so skip them.
          if (NylasValues.EventStatus.CANCELLED.equals(event.getStatus())) {
            return;
          }

          // For recurrence instances, store master id to fetch master event later.
          if (event.getMasterEventId() != null) {
            masterEventIds.add(event.getMasterEventId());
          }

          events.put(event.getId(), event);
        });

    // Fetch master events that haven't already been fetched (outside the sync timespan).
    // If any master event doesn't exist in Nylas, something is very wrong with the Nylas data,
    // but we'll log and continue, and the sync processing can deal with it further.
    // DO-LATER: See if Nylas will add a list filter to fetch multiple event_ids at once, a
    // filter to fetch only masters, or something more efficient to avoid multiple requests.
    masterEventIds.forEach(masterEventId -> {
      if (events.containsKey(masterEventId)) {
        return;
      }

      eventHelper.tryGetNylasEvent(client, new EventExternalId(masterEventId)).ifPresentOrElse(
          masterEvent -> events.put(masterEventId, masterEvent),
          () -> log.error("Master event {} not found in Nylas", masterEventId));
    });

    return events;
  }
}
