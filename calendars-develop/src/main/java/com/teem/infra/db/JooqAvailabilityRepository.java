package com.UoU.infra.db;

import static com.UoU.infra.jooq.tables.Calendar.CALENDAR;
import static com.UoU.infra.jooq.tables.Event.EVENT;
import static org.jooq.impl.DSL.if_;

import com.UoU.core.TimeSpan;
import com.UoU.core.calendars.AvailabilityRepository;
import com.UoU.core.calendars.AvailabilityRequest;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.events.EventTimeSpan;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectField;
import org.springframework.stereotype.Service;

/**
 * Repo for calendar availability queries.
 *
 * <p>All-day events will have their start and end dates pre-interpreted using the calendar timezone
 * so exact points in time can be stored in event start_at and end_at. Therefore, we only need to
 * calculate availability using start_at and end_at.
 *
 * <p>Note that for both availability windows and events, start times are inclusive, but end times
 * are exclusive. Therefore, events that abut a search window do not count as being inside it.
 * For example, here's how a search window should be used to determine if a calendar is free/busy:
 * <pre>
 * search window:                     --------
 * event inside window (BUSY):          ---
 * event aligns with start (BUSY):    ---
 * event aligns with end (BUSY):           ---
 * event overlaps start (BUSY):     ----
 * event overlaps end (BUSY):               ----
 * event abuts start (FREE):       ---
 * event abuts end (FREE):                    ---
 * </pre>
 */
@Service
@AllArgsConstructor
public class JooqAvailabilityRepository implements AvailabilityRepository {

  private final DSLContext dsl;

  /**
   * Returns boolean availability for the passed calendars and timespan.
   *
   * <p>Calendar ids that are invalid for the org will be excluded from the results.
   */
  @Override
  public Map<CalendarId, Boolean> getAvailability(AvailabilityRequest request) {
    return dsl
        .selectDistinct(CALENDAR.ID, if_(EVENT.ID.isNull(), true, false).as("is_available"))
        .from(CALENDAR)
        .leftJoin(EVENT).on(EVENT.CALENDAR_ID.eq(CALENDAR.ID)
            .and(EVENT.START_AT.lessThan(request.timeSpan().endAtUtcOffset()))
            .and(EVENT.END_AT.greaterThan(request.timeSpan().startAtUtcOffset())))
        .where(Conditions.orgMatches(CALENDAR, request.orgId()))
        .and(CALENDAR.ID.in(request.calendarIdValues()))
        .stream()
        .collect(Collectors.toMap(x -> new CalendarId(x.value1()), x -> x.value2()));
  }

  /**
   * Returns busy periods for the passed calendars and timespan.
   *
   * <p>Calendar ids that are invalid for the org will be excluded from the results.
   */
  @Override
  public Map<CalendarId, List<TimeSpan>> getBusyPeriods(AvailabilityRequest request) {
    var result = fetchBusyPeriods(request);
    return createBusyPeriodsMap(
        result,
        (timeSpan, record) -> timeSpan); // no mapping required, use timeSpan as is
  }

  /**
   * Returns busy periods with event details for the passed calendars and timespan.
   *
   * <p>Calendar ids that are invalid for the org will be excluded from the results.
   */
  @Override
  public Map<CalendarId, List<EventTimeSpan>> getDetailedBusyPeriods(AvailabilityRequest request) {
    var result = fetchBusyPeriods(request, EVENT.ID, EVENT.TITLE);
    return createBusyPeriodsMap(
        result,
        (timeSpan, record) -> new EventTimeSpan(
            timeSpan,
            record.getValue(EVENT.ID),
            record.getValue(EVENT.TITLE)));
  }

  /**
   * Fetches busy periods, optionally with extra event fields.
   *
   * <p>Result records will have at least these fields (plus any extra you pass):
   * - CALENDAR.ID
   * - EVENT.START_AT
   * - EVENT.END_AT
   */
  private Result<Record> fetchBusyPeriods(
      AvailabilityRequest request, SelectField<?>... extraEventFields) {

    return dsl
        .selectDistinct(Stream
            .concat(
                Stream.of(CALENDAR.ID, EVENT.START_AT, EVENT.END_AT),
                Stream.of(extraEventFields))
            .toList())
        .from(CALENDAR)
        .leftJoin(EVENT).on(EVENT.CALENDAR_ID.eq(CALENDAR.ID)
            .and(EVENT.START_AT.lessThan(request.timeSpan().endAtUtcOffset()))
            .and(EVENT.END_AT.greaterThan(request.timeSpan().startAtUtcOffset()))
            // Exclude recurrence masters because they will duplicate expanded instances:
            .and(EVENT.RECURRENCE.isNull()))
        .where(Conditions.orgMatches(CALENDAR, request.orgId()))
        .and(CALENDAR.ID.in(request.calendarIdValues()))
        .orderBy(CALENDAR.ID, EVENT.START_AT, EVENT.END_AT)
        .fetch();
  }

  /**
   * Creates a busy periods map of the desired type by applying the mapper to result timeSpans.
   *
   * <p>This requires the Result records to have these minimum fields in order:
   * - CALENDAR.ID
   * - EVENT.START_AT
   * - EVENT.END_AT
   */
  private <R extends Record, T> Map<CalendarId, List<T>> createBusyPeriodsMap(
      Result<R> result,
      BiFunction<TimeSpan, Record, T> mapper) {

    var map = new HashMap<CalendarId, List<T>>();

    for (var record : result) {
      var id = new CalendarId(record.getValue(CALENDAR.ID));
      if (!map.containsKey(id)) {
        map.put(id, new ArrayList<>());
      }

      Optional
          .ofNullable(record.getValue(1, OffsetDateTime.class))
          .map(start -> new TimeSpan(start, record.getValue(2, OffsetDateTime.class)))
          .ifPresent(timeSpan -> map.get(id).add(mapper.apply(timeSpan, record)));
    }

    return map;
  }
}
