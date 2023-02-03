package com.UoU.core.events;

import com.UoU.core.DataConfig;
import com.UoU.core.Fluent;
import com.UoU.core.OrgId;
import com.UoU.core.OrgMatcher;
import com.UoU.core.PagedItems;
import com.UoU.core.calendars.CalendarRepository;
import com.UoU.core.conferencing.ConferencingService;
import com.UoU.core.exceptions.IllegalOperationException;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.nylas.tasks.NylasTaskScheduler;
import com.UoU.core.validation.ValidatorWrapper;
import com.UoU.core.validation.ViolationException;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EventService {
  private final CalendarRepository calendarRepo;
  private final EventRepository eventRepo;
  private final NylasTaskScheduler nylasTaskScheduler;
  private final ValidatorWrapper validator;
  private final EventsConfig eventsConfig;
  private final EventPublisher eventPublisher;
  private final ConferencingService conferencingService;

  public PagedItems<Event> list(EventQuery query) {
    validator.validateAndThrow(query);
    return eventRepo.list(query);
  }

  public Event get(OrgId orgId, EventId id) {
    return Fluent.of(eventRepo.get(id))
        .also(x -> OrgMatcher.matchOrThrowNotFound(x.orgId(), orgId, Event.class))
        .get();
  }

  public void create(EventCreateRequest request) {
    validator.validateAndThrow(request);
    validateActivePeriod(request.when());
    if (request.recurrence().isMaster() || request.recurrence().isInstance()) {
      validateRecurrenceStart(request.when());
    }

    val orgId = request.orgId();
    if (calendarRepo.tryGetAccessInfo(request.calendarId())
        .filter(x -> x.isOrg(orgId) && x.isWritable())
        .isEmpty()) {
      throw ViolationException.forField(
          "calendarId", "Calendar not found or invalid for new events");
    }

    val eventId = request.id();
    val accountId = calendarRepo.getAccountId(request.calendarId());

    // Handle conferencing synchronously for now because there are some auth errors and things
    // that get complex to do async. If this becomes a bottleneck, we can make it async later.
    if (request.conferencing() != null) {
      request = conferencingService.addConferencingToEvent(request);
    }

    eventRepo.create(request);

    try {
      accountId.ifPresent(x -> nylasTaskScheduler.exportEventToNylas(x, eventId));
    } catch (Exception ex) {
      // If nylas export cannot be scheduled, remove the event so caller can try again rather than
      // allowing the event to be created and not exported so that it's in a weird state.
      eventRepo.delete(eventId);
      throw ex;
    }

    eventPublisher.eventCreated(List.of(eventId));
  }

  public void update(EventUpdateRequest request) {
    validator.validateAndThrow(request);
    validateActivePeriod(request.when());

    val event = eventRepo.get(request.id());
    event.accessInfo()
        .requireOrgOrThrowNotFound(request.orgId())
        .requireWritable();

    if (event.recurrence().isMaster() || event.recurrence().isInstance()) {
      validateRecurrenceStart(request.when());
    }
    if (event.recurrence().isMaster() && request.recurrence() == null) {
      throw ViolationException.forField(
          "recurrence", "Recurrence master must have recurrence info.");
    } else if (event.recurrence().isInstance() && request.recurrence() != null) {
      throw ViolationException.forField(
          "recurrence", "Recurrence instance cannot have recurrence master info.");
    }

    val updateRequest = request.withMatchingUpdateFieldsRemoved(event);
    eventRepo.update(updateRequest); // only updates if there are changes, else just logs

    if (!updateRequest.hasUpdates()) {
      return;
    }

    try {
      scheduleNylasUpdate(request.id());
    } finally {
      // Note: For recurrence masters, we do NOT need to publish the instances as changed here
      // because we do not actually change the instances. Rather, Nylas expands the recurrence
      // and so any instance changes are done there and pushed back to us; and at that time any
      // changed instances will have eventUpdated published.
      eventPublisher.eventUpdated(List.of(request.id()));
    }
  }

  /**
   * Validates that the event starts within the configured active period.
   */
  private void validateActivePeriod(When when) {
    // Use default tz for all-day events, which is close enough to validate the active period.
    val start = when.toUtcTimeSpan(() -> DataConfig.Calendars.DEFAULT_TIMEZONE).start();

    if (!eventsConfig.activePeriod().current().contains(start)) {
      throw ViolationException.forField(
          "when." + when.startPropertyName(),
          "Event must start within the current active period: "
          + eventsConfig.activePeriod().description());
    }
  }

  /**
   * Validates that the event start is suitable for a recurrence master and instance
   * (must be whole minutes).
   *
   * <p>Exchange does not support start times with remainder seconds for recurring events. Nylas
   * returns a message like "Exchange recurring events do not support start times that have
   * remainder seconds. Please use a start time of 1655218500 instead." We'll enforce the same rule
   * for all providers for consistency because it probably isn't a needed feature anyway.
   */
  private void validateRecurrenceStart(When when) {
    if (when instanceof When.TimeSpan) {
      val start = ((When.TimeSpan) when).startTime();
      val truncated = start.truncatedTo(ChronoUnit.MINUTES);
      if (!start.equals(truncated)) {
        throw ViolationException.forField(
            "when." + when.startPropertyName(),
            "Recurring event start must be rounded to whole minutes, such as: " + truncated);
      }
    }
  }

  /**
   * Deletes an event.
   *
   * <p>Event does *not* need to be writable to be deleted. For a readonly event, it will be
   * owned by another calendar, and deleting will decline our calendar's participation in
   * the event without touching the original event on the owner's calendar, which we do not have
   * permission to delete or modify anyway.
   */
  public void delete(EventRequest request) {
    validator.validateAndThrow(request);
    val coreIds = eventRepo.getCoreIds(request.id());
    OrgMatcher.matchOrThrowNotFound(request.orgId(), coreIds.orgId(), Event.class);

    val syncArgs = eventRepo.getAccountAndExternalIds(request.id());

    // For recurrence master, add instance ids to list of allIds to publish eventDeleted.
    val allIds = new HashSet<>(List.of(coreIds.id()));
    eventRepo.listRecurrenceInstanceIdPairs(coreIds.id()).forEach(x -> allIds.add(x.getLeft()));

    eventRepo.delete(request.id());

    try {
      syncArgs.getLeft().ifPresent(
          accountId -> syncArgs.getRight().ifPresent(
              externalId -> nylasTaskScheduler.deleteEventFromNylas(accountId, externalId)));
    } finally {
      eventPublisher.eventDeleted(
          request.orgId(), coreIds.calendarId(), allIds, request.dataSource());
    }
  }

  /**
   * Records an event checkin.
   *
   * <p>Event does *not* need to be writable to be checked into. Read-only events will be owned
   * by another calendar, and we cannot modify them, but we can update checkin/checkout metadata.
   */
  public void checkin(EventRequest request) {
    validator.validateAndThrow(request);
    val accessInfo = eventRepo.getAccessInfo(request.id())
        .requireOrgOrThrowNotFound(request.orgId());

    eventRepo.checkin(request.id(), request.dataSource());

    try {
      // Skip nylas sync for readonly events because there's nothing external we can update.
      // DO-LATER: See if Nylas will eventually support metadata on readonly events. This issue has
      // been raised with Nylas, and there is a feature request but no timeline for it.
      if (!accessInfo.isReadOnly()) {
        scheduleNylasUpdate(request.id());
      }
    } finally {
      eventPublisher.eventUpdated(List.of(request.id()));
    }
  }

  /**
   * Records an event checkout.
   *
   * <p>Event does *not* need to be writable to be checked into. Read-only events will be owned
   * by another calendar, and we cannot modify them, but we can update checkin/checkout metadata.
   */
  public void checkout(EventRequest request) {
    validator.validateAndThrow(request);
    val accessInfo = eventRepo.getAccessInfo(request.id())
        .requireOrgOrThrowNotFound(request.orgId());

    eventRepo.checkout(request.id(), request.dataSource());

    try {
      // Skip nylas sync for readonly events because there's nothing external we can update.
      if (!accessInfo.isReadOnly()) {
        scheduleNylasUpdate(request.id());
      }
    } finally {
      eventPublisher.eventUpdated(List.of(request.id()));
    }
  }

  private void scheduleNylasUpdate(EventId eventId) {
    eventRepo.getAccountId(eventId).ifPresent(
        accountId -> nylasTaskScheduler.exportEventToNylas(accountId, eventId));
  }
}
