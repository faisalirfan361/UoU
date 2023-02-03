package com.UoU.core.nylas.tasks;

import com.nylas.EventQuery;
import com.UoU.core.DataConfig;
import com.UoU.core.TimeSpan;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.events.EventExternalId;
import com.UoU.core.events.When;
import java.time.Duration;
import java.util.function.Consumer;
import lombok.val;

/**
 * Custom event query builder that ensures our defaults and does some more advanced things.
 */
class EventQueryBuilder {
  private final EventQuery query;

  public EventQueryBuilder() {
    this(new EventQuery());
  }

  public EventQueryBuilder(EventQuery baseQuery) {
    query = baseQuery.showCancelled(false).expandRecurring(false);
  }

  public EventQueryBuilder calendarExternalId(CalendarExternalId calendarExternalId) {
    query.calendarId(calendarExternalId.value());
    return this;
  }

  public EventQueryBuilder eventId(EventExternalId externalId) {
    query.eventId(externalId.value());
    return this;
  }

  /**
   * Filters events to those that start within the timeSpan.
   */
  public EventQueryBuilder startsWithin(TimeSpan timeSpan) {
    query.startsAfter(timeSpan.start().minusSeconds(1)) // start is inclusive, so -1 second
        .startsBefore(timeSpan.end()); // end is already exclusive
    return this;
  }

  /**
   * Expands recurring events around the When to include all events approximately When.
   *
   * <p>This uses approximate time filters based on the When so that any event matching the When is
   * included in the results, along with any other events starting and ending close to When. This
   * is useful when you need to use the list endpoint to find a specific event by time, especially
   * if you can't fetch it directly by id (like for non-override recurring instances).
   */
  public EventQueryBuilder expandRecurringApproximatelyAroundWhen(When when) {
    // For an all-day When, we don't have exact times, and we don't know the calendar timezone to
    // interpret exact times. Therefore, we'll interpret as default tz and expand recurring 25 hours
    // around When to make sure and capture the When in any timezone. For non-all day, we'll have
    // exact times, so we can approximate closer and expand recurring 10 minutes around When.
    val fudgeFactor  = when
        .toAllDayDateSpan()
        .map(x -> Duration.ofHours(25))
        .orElse(Duration.ofMinutes(10));
    val timeSpan = when.toUtcTimeSpan(() -> DataConfig.Calendars.DEFAULT_TIMEZONE);

    query.expandRecurring(true)
        .startsAfter(timeSpan.start().minus(fudgeFactor))
        .startsBefore(timeSpan.start().plus(fudgeFactor))
        .endsAfter(timeSpan.end().minus(fudgeFactor))
        .endsBefore(timeSpan.end().plus(fudgeFactor));

    return this;
  }

  /**
   * Perform an operation on the underlying EventQuery via the passed consumer.
   */
  public EventQueryBuilder and(Consumer<EventQuery> consumer) {
    consumer.accept(query);
    return this;
  }

  public EventQuery build() {
    return query;
  }
}
