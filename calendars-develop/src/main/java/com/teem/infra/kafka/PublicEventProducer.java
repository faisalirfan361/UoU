package com.UoU.infra.kafka;

import com.UoU.core.OrgId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.events.DataSource;
import com.UoU.core.events.Event;
import com.UoU.core.events.EventId;
import com.UoU.infra.avro.publicevents.EventChangeType;
import com.UoU.infra.kafka.mapping.PublicEventMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Producer for PUBLIC events that are meant for other teams and apps.
 */
@Service
@AllArgsConstructor
@Slf4j
public class PublicEventProducer {
  private final Sender sender;
  private final TopicNames.PublicEvents topicNames;
  private final PublicEventMapper mapper;

  /**
   * Produces a batch of PUBLIC EventChanged (created) events.
   *
   * <p>Each event will be produced separately with any error logged, and
   * EventChangedProducerException will be thrown at the end if any failed.
   */
  public void eventCreated(Iterable<Event> batch) {
    sendEventChangedBatch(EventChangeType.created, batch, event -> event.createdFrom());
  }

  /**
   * Produces a batch of PUBLIC EventChanged (updated) events.
   *
   * <p>Each event will be produced separately with any error logged, and
   * EventChangedProducerException will be thrown at the end if any failed.
   */
  public void eventUpdated(Iterable<Event> batch) {
    sendEventChangedBatch(EventChangeType.updated, batch, event -> event.updatedFrom());
  }

  private void sendEventChangedBatch(
      EventChangeType changeType,
      Iterable<Event> batch,
      Function<Event, DataSource> changeSourceSelector) {

    // These events are important, so try to send each separately and log each failed id.
    val exceptions = new HashMap<EventId, Exception>();

    for (val event : batch) {
      try {
        val changeSource = changeSourceSelector.apply(event);
        val value = mapper.toEventChangedAvro(event, changeType, changeSource);
        sender.send(topicNames.getEventChanged(), event.id().value().toString(), value);

        log.debug("Produced PUBLIC EventChanged ({}): orgId={}, calendarId={}, eventId={}, src={}",
            changeType, event.orgId().value(), event.calendarId().value(), event.id().value(),
            changeSource);

      } catch (Exception ex) {
        exceptions.put(event.id(), ex);
        log.error("Failed producing PUBLIC EventChanged ({}) for: {}",
            changeType, event.id().value(), ex);
      }
    }

    if (!exceptions.isEmpty()) {
      throw new EventChangedProducerException(changeType, exceptions);
    }
  }

  /**
   * Produces a batch of PUBLIC EventChanged (deleted) events.
   *
   * <p>Each event will be produced separately with any error logged, and
   * EventChangedProducerException will be thrown at the end if any failed.
   */
  public void eventDeleted(
      OrgId orgId, CalendarId calendarId, Iterable<EventId> eventIds, DataSource dataSource) {

    // These events are important, so try to send each separately and log each failed id.
    val exceptions = new HashMap<EventId, Exception>();

    for (val id : eventIds) {
      try {
        val value = mapper.toEventChangedDeletedAvro(orgId, calendarId, id, dataSource);
        sender.send(topicNames.getEventChanged(), id.value().toString(), value);

        log.debug(
            "Produced PUBLIC EventChanged (deleted): orgId={}, calendarId={}, eventId={}, src={}",
            orgId.value(), calendarId.value(), id.value(), dataSource);
      } catch (Exception ex) {
        exceptions.put(id, ex);
        log.error("Failed producing PUBLIC EventChanged (deleted) for: {}", id.value(), ex);
      }
    }

    if (!exceptions.isEmpty()) {
      throw new EventChangedProducerException(EventChangeType.deleted, exceptions);
    }
  }

  private static class EventChangedProducerException extends RuntimeException {
    private final EventChangeType changeType;
    private final Map<EventId, Exception> exceptions;

    public EventChangedProducerException(
        EventChangeType changeType, Map<EventId, Exception> exceptions) {
      super("Failed to produce PUBLIC EventChanged (" + changeType + ") for events: "
          + exceptions.keySet().stream()
          .map(x -> x.value().toString())
          .collect(Collectors.joining(", "))
          + ". First error: " + exceptions.values().stream()
          .findFirst()
          .map(x -> x.getMessage())
          .orElse("none"));
      this.changeType = changeType;
      this.exceptions = exceptions;
    }
  }
}
