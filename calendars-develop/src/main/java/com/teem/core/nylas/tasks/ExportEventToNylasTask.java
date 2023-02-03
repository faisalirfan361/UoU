package com.UoU.core.nylas.tasks;

import com.nylas.NylasAccount;
import com.UoU.core.Task;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.events.DataSource;
import com.UoU.core.events.Event;
import com.UoU.core.events.EventExternalId;
import com.UoU.core.events.EventId;
import com.UoU.core.events.EventPublisher;
import com.UoU.core.events.EventRepository;
import com.UoU.core.events.EventUpdateRequest;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.exceptions.ReadOnlyException;
import com.UoU.core.nylas.ExternalEtag;
import com.UoU.core.nylas.ExternalEtagRepository;
import com.UoU.core.nylas.RecurrenceInfo;
import com.UoU.core.nylas.mapping.NylasEventMapper;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Outbound: Exports local event to Nylas.
 *
 * <p>This does NOT check the events active sync period and always exports. Events outside the
 * active period should be prevented from being saved before getting to this point, but it doesn't
 * make sense to keep events locally and then not sync them.
 *
 * <p>For recurrence masters, this does not update any of the instances that may be changed;
 * rather, nylas should send a webhook when those instance changes are ready so
 * {@link ImportEventFromNylasTask} should handle the recurrence instance changes. For Google, it
 * seems like the recurrence instances are available immediately (Nylas must expand instances
 * themselves), but for MS the instance expansion takes a moment. To keep things consistent, we'll
 * rely on the same flow for both, but if needed, eventually, we could optimize the Google export
 * by syncing the instances right away.
 */
@Service
@AllArgsConstructor
@Slf4j
public class ExportEventToNylasTask implements Task<ExportEventToNylasTask.Params> {
  private final EventHelper eventHelper;
  private final EventRepository eventRepo;
  private final ExternalEtagRepository etagRepo;
  private final NylasEventMapper mapper;
  private final EventPublisher eventPublisher;


  public record Params(
      @NonNull AccountId accountId,
      @NonNull EventId eventId
  ) {
  }

  @Override
  @SneakyThrows
  public void run(Params params) {
    val localEvent = eventRepo.get(params.eventId());

    if (localEvent.isReadOnly()) {
      // Export of read-only events should be prevented before here, but just in case, provide a
      // good exception rather than relying on the Nylas error, which is a bit confusing.
      // Technically, Nylas does allow updating the participants on a readonly event, but this
      // doesn't seem to work right for Exchange because it immediately gets put back to how it was.
      // Also, the Java SDK doesn't seem to allow updating just the participants. Therefore, we just
      // won't support creates or updates on readonly events at all for the time being.
      // TODO: Find out from Nylas if the above participants behavior is expected or a bug.
      throw new ReadOnlyException(
          "Event is read-only and cannot be exported to Nylas: " + localEvent.id());
    }

    val client = eventHelper.createNylasClient(params.accountId());
    val existingNylasEvent = Optional
        .ofNullable(localEvent.externalId())
        .flatMap(externalId -> localEvent.recurrence().isInstance()
            ? tryGetNylasRecurrenceInstance(client, localEvent)
            : eventHelper.tryGetNylasEvent(client, externalId));

    // If we have a recurrence instance but can't find the Nylas event, we can't proceed.
    // Nylas creates all the recurrence instances, so we can only update them.
    // This shouldn't be allowed to happen, so there may be some data corruption.
    if (existingNylasEvent.isEmpty() && localEvent.recurrence().isInstance()) {
      throw new NotFoundException(
          "Nylas event not found for recurrence instance: " + localEvent.id());
    }

    com.nylas.Event nylasEvent;
    EventExternalId externalId;

    if (existingNylasEvent.isPresent()) {
      val existingNylasEventValue = existingNylasEvent.orElseThrow();
      mapper.updateNylasEvent(existingNylasEventValue, localEvent);

      nylasEvent = client.events().update(existingNylasEventValue, true);
      externalId = new EventExternalId(nylasEvent.getId());

      // Make db update request to handle the export resulting in any immediate changes.
      // This can happen from Nylas normalizing data so it's different from what we sent.
      var updateRequest = mapper.toUpdateRequestModel(nylasEvent, localEvent);

      // Support changing recurrence instance from non-override to override while keeping the same
      // local id for continuity. In this case, the external_id and is_recurrence_override
      // will change because Nylas deletes the old, non-override instance and creates a new event.
      // If we do this, we can remove the externalId change from updateRequest since it's done.
      val isRecurrenceInstanceChangedToOverride =
          new RecurrenceInfo(existingNylasEventValue).isNonOverrideInstance()
              && new RecurrenceInfo(nylasEvent).isOverrideInstance();
      if (isRecurrenceInstanceChangedToOverride) {
        eventRepo.updateRecurrenceInstance(localEvent.id(), externalId, true, DataSource.PROVIDER);
        updateRequest = updateRequest.withUpdateFieldsRemoved(
            EventUpdateRequest.UpdateField.EXTERNAL_ID);
      }

      eventRepo.update(updateRequest); // only updates db if there are changes, else just logs

      // When updating a nylas event, we may get an immediate webhook for normalizing data, but we
      // may not, so we need to publish eventChanged if there are changes from the nylas update.
      if (isRecurrenceInstanceChangedToOverride || updateRequest.hasUpdates()) {
        eventPublisher.eventUpdated(List.of(localEvent.id()));
      }
    } else {
      val calendarExternalId = eventHelper.getCalendarExternalId(localEvent.calendarId());
      nylasEvent = client.events().create(
          mapper.toNylasEvent(localEvent, calendarExternalId), true);
      externalId = new EventExternalId(nylasEvent.getId());

      try {
        eventRepo.update(mapper.toUpdateRequestModel(nylasEvent, localEvent));
      } catch (Exception ex) {
        // The nylas event has been created, but our update failed. This is a bad state to be in
        // because the externalId will not be set locally, so retries at this point would create
        // duplicate events in nylas. We will try once more to set only the externalId to tie the
        // local and nylas events together. If that works, we can try a subsequent update. The
        // id update may also fail if something is really wrong with the db, but it could bypass
        // some mapping and data specific exceptions that could break the update. The reason why
        // we don't just delete the nylas event and start over is because notifications could have
        // already been sent to participants, and we don't want to cause a lot of changes on the
        // provider calendar if possible.
        eventRepo.updateExternalId(localEvent.id(), externalId, DataSource.PROVIDER);
        eventRepo.update(mapper.toUpdateRequestModel(nylasEvent, localEvent));
      }

      // Note: When creating a nylas event, skip eventPublisher.eventUpdated() because only the
      // externalId should be updated, which is not part of the EventChanged contract, and also the
      // provider will update the icaluid (at least) and cause a webhook update momentarily anyway.
    }

    // Save the etag so any subsequent imports without any changes can be skipped.
    val externalEtag = new ExternalEtag(nylasEvent);
    etagRepo.save(externalId, externalEtag);

    log.debug("Exported event to Nylas: {}, {}, existedInNylas={}",
        localEvent.id(), externalId, existingNylasEvent.isPresent());
  }

  /**
   * Gets the Nylas recurrence instance event if it exists, including if it's a non-override.
   *
   * <p>This supports fetching non-override recurrence instances as well, which needs to be done a
   * different way because the Nylas API does not allow fetching these instances the normal way. The
   * passed localEvent is used to determine how to fetch the event from Nylas.
   */
  @SneakyThrows
  private Optional<com.nylas.Event> tryGetNylasRecurrenceInstance(
      NylasAccount client, Event localEvent) {

    val externalIdOptional = Optional.ofNullable(localEvent.externalId());
    if (externalIdOptional.isEmpty() || !localEvent.recurrence().isInstance()) {
      return Optional.empty();
    }

    val externalId = externalIdOptional.orElseThrow();
    val instance = localEvent.recurrence().getInstance();

    // For override events, fetch the event directly by id like any other event:
    if (instance.isOverride()) {
      return eventHelper.tryGetNylasEvent(client, externalId);
    }

    // But for non-override recurring instances, we have to go through the list endpoint to expand
    // recurring events because these events are not currently fetchable by id.
    // DO-LATER: This is a huge pain. I brought it up with Nylas, so check back sometime later and
    // see if they'll add a better way to fetch these non-override instances. Ideally, we could
    // fetch any event directly by id and not have to have special handling at all.
    val masterExternalId = eventRepo.getExternalId(instance.masterId())
        .orElseThrow(() -> new NotFoundException(
            "Master event external id not found: " + instance.masterId()));

    return client.events()
        .list(new EventQueryBuilder()
            .eventId(masterExternalId)
            .expandRecurringApproximatelyAroundWhen(localEvent.when())
            .build())
        .fetchAll()
        .stream()
        .filter(x -> x.getId().equals(externalId.value()))
        .findFirst();
  }
}
