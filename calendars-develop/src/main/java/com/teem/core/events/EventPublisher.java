package com.UoU.core.events;

import com.UoU.core.OrgId;
import com.UoU.core.calendars.CalendarId;
import java.util.Collection;

/**
 * Producer of events for internal use (inside this app).
 *
 * <p>In some cases, internal events will be consumed by us, enriched, and re-published as PUBLIC
 * events. But when no enrichment is needed, this will produce PUBLIC events directly. As a
 * user of this interface, you don't really need to know the destination; just use this and let
 * it route rather than trying to produce the PUBLIC events directly.
 */
public interface EventPublisher {
  void eventCreated(Collection<EventId> eventIds);

  void eventUpdated(Collection<EventId> eventIds);

  void eventDeleted(
      OrgId orgId, CalendarId calendarId, Collection<EventId> eventIds, DataSource dataSource);
}
