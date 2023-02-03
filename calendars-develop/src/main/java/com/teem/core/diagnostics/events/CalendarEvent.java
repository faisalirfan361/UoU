package com.UoU.core.diagnostics.events;

import com.UoU.core.events.EventExternalId;
import com.UoU.core.events.EventId;
import java.time.Instant;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Diagnostic events related to calendars and events.
 */
public interface CalendarEvent extends DiagnosticEvent {

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  class EventCreated extends BaseEvent implements CalendarEvent {
    public EventCreated(EventId id, String title, Instant start) {
      super(
          "Event was created and stored locally.",
          Map.of("event", Map.of("id", id.value(), "title", title, "start", start)));
    }
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  class EventExported extends BaseEvent implements CalendarEvent {
    public EventExported(EventId id, EventExternalId externalId) {
      super(
          "Event was exported to begin sync to external calendar provider.",
          Map.of("event", Map.of("id", id.value(), "externalId", externalId.value())));
    }
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  class EventSyncedFromProvider extends BaseEvent implements CalendarEvent {
    public EventSyncedFromProvider(EventId id, EventExternalId externalId, String icalUid) {
      super(
          "Event was synced from external calendar provider.",
          Map.of("event", Map.of(
              "id", id.value(), "externalId", externalId.value(), "icalUid", icalUid
          )));
    }
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  class EventDeleted extends BaseEvent implements CalendarEvent {
    public EventDeleted(EventId id) {
      super(
          "Event deleted. (Delete from external calendar provider may take a few moments.)",
          Map.of("event", Map.of("id", id.value())));
    }
  }
}
