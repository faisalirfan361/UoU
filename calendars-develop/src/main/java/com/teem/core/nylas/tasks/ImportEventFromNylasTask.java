package com.UoU.core.nylas.tasks;

import com.nylas.NylasAccount;
import com.UoU.core.DataConfig;
import com.UoU.core.Fluent;
import com.UoU.core.Lazy;
import com.UoU.core.OrgId;
import com.UoU.core.Task;
import com.UoU.core.TimeSpan;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarId;
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
import com.UoU.core.nylas.RecurrenceInfo;
import com.UoU.core.nylas.mapping.NylasEventMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Inbound: Imports an event from Nylas to local.
 *
 * <p>This respects the events active sync period and skips creating events outside the period,
 * except for master recurring events that have an instance inside the period.
 *
 * <p>If the event has been deleted from Nylas, this will do the same thing as
 * {@link HandleEventDeleteFromNylasTask}.
 *
 * <p>This uses {@link com.UoU.core.nylas.InboundSyncLocker} to skip sync when another major
 * inbound sync is occurring, which will help prevent race conditions and unnecessary operations.
 */
@Service
@AllArgsConstructor
@Slf4j
public class ImportEventFromNylasTask implements Task<ImportEventFromNylasTask.Params> {
  private final EventHelper eventHelper;
  private final EventRepository eventRepo;
  private final ExternalEtagRepository etagRepo;
  private final NylasEventMapper mapper;
  private final EventPublisher eventPublisher;
  private final InboundSyncLocker inboundSyncLocker;
  private final HandleEventDeleteFromNylasTask handleEventDeleteFromNylasTask;

  public record Params(
      @NonNull AccountId accountId,
      @NonNull EventExternalId externalId
  ) {
  }

  @Override
  public void run(Params params) {
    if (inboundSyncLocker.isAccountLocked(params.accountId())) {
      log.debug("Inbound sync locked for {}. Skipping: Import event {} from Nylas",
          params.accountId(), params.externalId());
      return;
    }

    val client = eventHelper.createNylasClient(params.accountId());
    val nylasEventOptional = eventHelper.tryGetNylasEvent(client, params.externalId());

    // If the event is missing from nylas or marked as cancelled, delegate to the delete task
    // and end. Nylas updates events to "cancelled" for most deletes, but then they may also send
    // another webhook where the event is removed from their API altogether. In either case, we just
    // need to make sure our record is removed, and then we're in sync with nylas. If the event is a
    // master recurring event, this will delete the entire series.
    val isDeletedOrCancelled = nylasEventOptional
        .map(x -> NylasValues.EventStatus.CANCELLED.equals(x.getStatus()))
        .orElse(true);
    if (isDeletedOrCancelled) {
      handleEventDeleteFromNylasTask.run(
          new HandleEventDeleteFromNylasTask.Params(params.accountId(), params.externalId()));
      log.debug("Imported (deleted) event from Nylas {}", params.externalId());
      return;
    }

    val syncPeriod = eventHelper.getCurrentActivePeriod();
    val nylasEvent = nylasEventOptional.orElseThrow();
    val nylasRecurrenceInfo = new RecurrenceInfo(nylasEvent);
    val nylasRecurrenceInstances = new Lazy<>(() ->
        nylasRecurrenceInfo.isMaster()
            ? getNylasRecurrenceInstances(client, params.externalId(), syncPeriod).toList()
            : List.<com.nylas.Event>of());

    val localEvent = new Lazy<>(() -> eventRepo.tryGetByExternalId(params.externalId()));
    val localId = new Lazy<>(() -> localEvent.get().map(x -> x.id()).orElseGet(EventId::create));
    val calendarExternalId = new CalendarExternalId(nylasEvent.getCalendarId());
    val calendar = new Lazy<>(() -> eventHelper.getCalendarByExternalId(calendarExternalId));
    val calendarId = calendar.map(x -> x.id());
    val orgId = calendar.map(x -> x.orgId());

    val externalEtag = new ExternalEtag(nylasEvent);
    val externalEtagMatches = etagRepo
        .get(params.externalId())
        .map(etag -> etag.equals(externalEtag))
        .orElse(false);

    if (externalEtagMatches) {
      log.debug("Skipping import of event {} with matching external etag {}",
          nylasEvent.getId(), externalEtag);
    } else {
      localEvent.get().ifPresentOrElse(
          event -> {
            val updateRequest = mapper.toUpdateRequestModel(nylasEvent, event);
            eventRepo.update(updateRequest); // only updates if there are changes, else just logs

            if (updateRequest.hasUpdates()) {
              eventPublisher.eventUpdated(List.of(event.id()));
            }

            log.debug("Imported (updated) event from Nylas: {}, {}, {}",
                event.id(), event.externalId(), nylasRecurrenceInfo);
          },
          () -> {
            // Skip creating new events for read-only calendars because there are entire calendars
            // we ignore from nylas except for the main calendar record, which differentiates
            // a read-only calendar we ignore from a calendar that doesn't exist and should error.
            // DO-LATER: Optimize this so we don't have to do this extra db call every time.
            if (calendar.get().isReadOnly()) {
              log.debug("Skipping event import for read-only calendar: {}", calendarExternalId);
              return;
            }

            // For most events, skip creating when event start is outside our sync period.
            // For master recurring events, even if start is outside the sync period, still create
            // if there are any recurrence instances within the sync period.
            val start = mapper
                .toWhenModel(nylasEvent.getWhen())
                .toUtcTimeSpan(
                    () -> DataConfig.Calendars.DEFAULT_TIMEZONE) // default is close enough
                .start();
            if (!syncPeriod.contains(start) && nylasRecurrenceInstances.get().isEmpty()) {
              log.debug("Skipping import of event {} outside allowed timespan on calendar: {}",
                  nylasEvent.getId(), calendarExternalId);
              return;
            }

            // If the event has a master event id, it's a recurrence instance. In that case, lookup
            // the master id and create an instance tied to the master. Else create normal event.
            val createRequest = nylasRecurrenceInfo
                .withInstanceMasterEventId()
                .map(masterNylasId -> eventRepo.getId(new EventExternalId(masterNylasId))
                    .orElseGet(() -> {
                      // If the master event is not found, something is really wrong. Rather than
                      // fail the import, import the event without the master id so it will be
                      // treated as non-recurring. But log the error so we can see if this ever
                      // happens, and then we'll possibly need a way to resolve it.
                      // DO-LATER: Change this to import the master if not found so we handle cases
                      // where we process instance webhook before the master (which should be rare).
                      log.error("Import event failure: master event {} not found for instance {}",
                          masterNylasId, nylasEvent.getId());
                      return null;
                    }))
                .map(masterLocalId -> mapper.toCreateRequestModel(
                    nylasEvent, localId.get(), masterLocalId, calendarId.get(), orgId.get()))
                .orElseGet(() -> mapper.toCreateRequestModel(
                    nylasEvent, localId.get(), calendarId.get(), orgId.get()));

            eventRepo.create(createRequest);
            eventPublisher.eventCreated(List.of(createRequest.id()));

            log.debug("Imported (created) event from Nylas: {}, {}, {}",
                localId, params.externalId(), nylasRecurrenceInfo);
          });
    }

    // Always save the etag, even if the value is the same, so the expiration gets updated.
    etagRepo.save(params.externalId(), externalEtag);

    // For master recurring events, we need to import expanded recurrence instances.
    // This needs to happen even if the master etag matches because instances aren't part of etag.
    if (nylasRecurrenceInfo.isMaster() && !calendar.get().isReadOnly()) {
      val isMasterNew = localEvent.get().isEmpty();
      importNylasRecurrenceInstances(
          orgId.get(), calendarId.get(), localId.get(),
          isMasterNew, nylasRecurrenceInstances.get());
    }
  }

  @SneakyThrows
  private Stream<com.nylas.Event> getNylasRecurrenceInstances(
      NylasAccount client, EventExternalId masterExternalId, TimeSpan syncPeriod) {

    // We exclude cancelled in the query, but we also have to remove cancelled from the results
    // because Nylas always returns cancelled instances regardless of query.
    return client.events()
        .list(new EventQueryBuilder()
            .eventId(masterExternalId)
            .startsWithin(syncPeriod)
            .and(x -> x.expandRecurring(true))
            .build())
        .fetchAll()
        .stream()
        .filter(x -> !NylasValues.EventStatus.CANCELLED.equals(x.getStatus()));
  }

  private void importNylasRecurrenceInstances(
      OrgId orgId, CalendarId calendarId, EventId masterId, boolean isMasterNew,
      List<com.nylas.Event> instances) {

    val newExternalEtags = new HashMap<EventExternalId, ExternalEtag>();

    // If the master is new, we can't have any local instances yet, so just create and exit.
    if (isMasterNew) {
      val createBatch = new HashMap<EventId, EventCreateRequest>();
      instances.forEach(event -> {
        val id = EventId.create();
        createBatch.put(id, mapper.toCreateRequestModel(event, id, masterId, calendarId, orgId));
        newExternalEtags.put(new EventExternalId(event.getId()), new ExternalEtag(event));
      });

      eventRepo.batchCreate(createBatch.values());
      eventPublisher.eventCreated(createBatch.keySet());
      etagRepo.save(newExternalEtags);
      return;
    }

    // Master is not new, so do a full sync of the instances.
    val nylasEventMap = new HashMap<String, com.nylas.Event>();
    val externalIds = new HashSet<EventExternalId>();
    instances.forEach(event -> {
      nylasEventMap.put(event.getId(), event);
      externalIds.add(new EventExternalId(event.getId()));
    });

    val localEvents = eventRepo.listRecurrenceInstances(masterId)
        .collect(Collectors.toMap(x -> x.externalId().value(), x -> x));

    val createBatch = new HashMap<EventId, EventCreateRequest>();
    val updateBatch = new ArrayList<EventUpdateRequest>();
    val updateIdsWithChanges = new ArrayList<EventId>();

    nylasEventMap.values().forEach(nylasEvent -> {
      Optional
          .ofNullable(localEvents.get(nylasEvent.getId()))
          .ifPresentOrElse(
              localEvent -> {
                val updateRequest = mapper.toUpdateRequestModel(nylasEvent, localEvent);
                updateBatch.add(updateRequest);
                if (updateRequest.hasUpdates()) {
                  updateIdsWithChanges.add(localEvent.id());
                }
              },
              () -> Fluent
                  .of(EventId.create())
                  .also(id -> createBatch.put(
                      id,
                      mapper.toCreateRequestModel(nylasEvent, id, masterId, calendarId, orgId))));

      newExternalEtags.put(
          new EventExternalId(nylasEvent.getId()), new ExternalEtag(nylasEvent));
    });

    val deleteBatch = new ArrayList<EventId>();
    val deleteBatchExternalIds = new HashSet<EventExternalId>();
    localEvents.entrySet().stream()
        .filter(x -> !nylasEventMap.containsKey(x.getKey()))
        .forEach(x -> {
          deleteBatch.add(x.getValue().id());
          deleteBatchExternalIds.add(x.getValue().externalId());
        });

    eventRepo.batchCreate(createBatch.values());
    eventPublisher.eventCreated(createBatch.keySet());

    eventRepo.batchUpdate(updateBatch);
    eventPublisher.eventUpdated(updateIdsWithChanges);

    eventRepo.batchDelete(deleteBatch);
    eventPublisher.eventDeleted(orgId, calendarId, deleteBatch, DataSource.PROVIDER);

    etagRepo.save(newExternalEtags);
    etagRepo.tryDelete(deleteBatchExternalIds);

    log.debug(
        "Imported recurrence instances for master {}: create={}, update={}, del={}, etags=+{}/-{}",
        masterId, createBatch.size(), updateBatch.size(), deleteBatch.size(),
        newExternalEtags.size(), deleteBatchExternalIds.size());
  }
}
