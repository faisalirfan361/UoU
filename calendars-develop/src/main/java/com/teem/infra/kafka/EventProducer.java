package com.UoU.infra.kafka;

import com.UoU.core.OrgId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.events.DataSource;
import com.UoU.core.events.EventId;
import com.UoU.core.events.EventPublisher;
import com.UoU.infra.avro.events.EventChangeType;
import com.UoU.infra.avro.events.EventChanged;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Producer of events for internal use (inside this app).
 */
@Service
@AllArgsConstructor
public class EventProducer implements EventPublisher {
  private final Sender sender;
  private final TopicNames.Events eventTopicNames;
  private final PublicEventProducer publicEventProducer;

  /**
   * Produces internal EventChanged (created) events.
   *
   * <p>These internal events are meant to be consumed by us, enriched, and re-published as PUBLIC.
   */
  @Override
  public void eventCreated(Collection<EventId> eventIds) {
    sendEventChanged(EventChangeType.created, eventIds);
  }

  /**
   * Produces internal EventChanged (updated) events.
   *
   * <p>These internal events are meant to be consumed by us, enriched, and re-published as PUBLIC.
   */
  @Override
  public void eventUpdated(Collection<EventId> eventIds) {
    sendEventChanged(EventChangeType.updated, eventIds);
  }

  private void sendEventChanged(EventChangeType changeType, Collection<EventId> eventIds) {
    if (eventIds.isEmpty()) {
      return;
    }

    val idList = eventIds.stream().map(x -> x.value().toString()).toList();
    sender.send(
        eventTopicNames.getEventChanged(),
        EventChanged.newBuilder()
            .setChangeType(changeType)
            .setEventIds(idList)
            .build());
  }

  /**
   * Produces PUBLIC EventChanged (deleted) events.
   *
   * <p>Since these events don't need any enrichment, they are published directly as PUBLIC events.
   */
  @Override
  public void eventDeleted(
      OrgId orgId, CalendarId calendarId, Collection<EventId> eventIds, DataSource dataSource) {
    publicEventProducer.eventDeleted(orgId, calendarId, eventIds, dataSource);
  }
}
