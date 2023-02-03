package com.UoU.infra.db;

import static com.UoU.infra.jooq.Tables.CALENDAR;
import static com.UoU.infra.jooq.Tables.EVENT;
import static com.UoU.infra.jooq.Tables.PARTICIPANT;
import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.UoU.core.Fluent;
import com.UoU.core.Noop;
import com.UoU.core.OrgId;
import com.UoU.core.PagedItems;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.events.CoreIds;
import com.UoU.core.events.DataSource;
import com.UoU.core.events.Event;
import com.UoU.core.events.EventAccessInfo;
import com.UoU.core.events.EventCreateRequest;
import com.UoU.core.events.EventExternalId;
import com.UoU.core.events.EventId;
import com.UoU.core.events.EventQuery;
import com.UoU.core.events.EventRepository;
import com.UoU.core.events.EventUpdateRequest;
import com.UoU.core.events.Recurrence;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.infra.db.mapping.JooqEventMapper;
import com.UoU.infra.db.mapping.JooqParticipantMapper;
import com.UoU.infra.jooq.tables.records.EventRecord;
import com.UoU.infra.jooq.tables.records.ParticipantRecord;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.UpdateSetMoreStep;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class JooqEventRepository implements EventRepository {
  private final DSLContext dsl;
  private final JooqEventMapper eventMapper;
  private final JooqParticipantMapper participantMapper;
  private final ExceptionHelper exceptionHelper = new ExceptionHelper(Event.class);

  @Override
  public PagedItems<Event> list(EventQuery query) {
    // Cursor paging fields: START_AT, ID, filterChecksum
    // The filterChecksum is used to make sure filtering remains stable through the pages.
    val cursor = Cursor.decoder().decodeThreeAndMap(
        query.page().cursor(),
        (startAt, id, filterChecksum) -> {
          query.validateFilterChecksum(filterChecksum);
          return Pair.of(OffsetDateTime.parse(startAt), UUID.fromString(id));
        });
    val when = Optional.ofNullable(query.when());

    val events = Fluent
        .of(selectEvents(query.orgId(), query.calendarId()))
        .ifThenAlso(cursor, (x, cursorValue) -> x.and(
            EVENT.START_AT.gt(cursorValue.getLeft())
                .or(EVENT.START_AT.eq(cursorValue.getLeft())
                    .and(EVENT.ID.gt(cursorValue.getRight())))))
        .ifThenAlso(query.expandRecurring(), x -> x.and(
            // expandRecurring==true: hide recurrence masters, return only instances
            EVENT.RECURRENCE.isNull()))
        .ifThenAlso(!query.expandRecurring(), x -> x.and(
            // expandRecurring==false: return only recurrence masters and instance overrides.
            // Non-override instances can be generated from the schedule so are not returned.
            EVENT.RECURRENCE_MASTER_ID.isNull()
                .or(EVENT.IS_RECURRENCE_OVERRIDE)))
        .ifThenAlso(when.map(x -> x.startsBefore()), (x, startsBefore) -> x.and(
            EVENT.START_AT.lt(startsBefore.atOffset(ZoneOffset.UTC))))
        .ifThenAlso(when.map(x -> x.startsAfter()), (x, startsAfter) -> x.and(
            EVENT.START_AT.gt(startsAfter.atOffset(ZoneOffset.UTC))))
        .ifThenAlso(when.map(x -> x.endsBefore()), (x, endsBefore) -> x.and(
            EVENT.END_AT.lt(endsBefore.atOffset(ZoneOffset.UTC))))
        .ifThenAlso(when.map(x -> x.endsAfter()), (x, endsAfter) -> x.and(
            EVENT.END_AT.gt(endsAfter.atOffset(ZoneOffset.UTC))))
        .get()
        .orderBy(EVENT.START_AT, EVENT.ID)
        .limit(Math.max(2, query.page().limit() + 1)) // fetch +1 so we know if next page exists
        .fetch();

    val nextCursor = Optional
        .of(events)
        .filter(x -> x.size() > query.page().limit() && x.size() >= 2)
        .map(x -> x.get(x.size() - 2)) // last in page, accounting for one extra
        .map(x -> new Cursor(x.getStartAt(), x.getId(), query.toFilterChecksum()).encode());
    nextCursor.ifPresent(x -> events.remove(events.size() - 1)); // remove one extra

    val participants = fetchEventParticipants(events);

    return new PagedItems<>(
        events.map(x -> eventMapper.toModel(x, participants.get(x.getId()))),
        nextCursor.orElse(null));
  }

  @Override
  public Stream<Event> listById(Collection<EventId> ids) {
    val events = dsl
        .selectFrom(EVENT)
        .where(EVENT.ID.in(ids.stream().map(x -> x.value()).toList()))
        .fetch();
    val participants = fetchEventParticipants(events);

    return events.stream().map(x -> eventMapper.toModel(x, participants.get(x.getId())));
  }

  @Override
  public Stream<Event> listByCalendar(OrgId orgId, CalendarId calendarId) {
    val events = selectEvents(orgId, calendarId)
        .orderBy(EVENT.START_AT, EVENT.ID)
        .fetch();
    val participants = fetchEventParticipants(events);

    return events.stream().map(x -> eventMapper.toModel(x, participants.get(x.getId())));
  }

  @Override
  public Stream<Pair<EventId, Optional<EventExternalId>>> listRecurrenceInstanceIdPairs(
      EventId masterEventId) {
    val events = dsl
        .select(EVENT.ID, EVENT.EXTERNAL_ID)
        .from(EVENT)
        .where(EVENT.RECURRENCE_MASTER_ID.eq(masterEventId.value()))
        .orderBy(EVENT.START_AT, EVENT.ID)
        .fetch();

    return events.stream().map(x -> Pair.of(
        new EventId(x.value1()),
        Optional.ofNullable(x.value2()).map(EventExternalId::new)));
  }

  @Override
  public Stream<Event> listRecurrenceInstances(EventId masterEventId) {
    val events = dsl
        .selectFrom(EVENT)
        .where(EVENT.RECURRENCE_MASTER_ID.eq(masterEventId.value()))
        .orderBy(EVENT.START_AT, EVENT.ID)
        .fetch();
    val participants = fetchEventParticipants(events);

    return events.stream().map(x -> eventMapper.toModel(x, participants.get(x.getId())));
  }

  private SelectConditionStep<EventRecord> selectEvents(OrgId orgId, CalendarId calendarId) {
    return dsl
        .selectFrom(EVENT)
        .where(Conditions.orgMatches(EVENT, orgId))
        .and(EVENT.CALENDAR_ID.eq(calendarId.value()));
  }

  private Map<UUID, Result<ParticipantRecord>> fetchEventParticipants(Result<EventRecord> events) {
    return Optional
        .of(events.map(x -> x.getId()))
        .map(eventIds -> dsl
            .selectFrom(PARTICIPANT)
            .where(PARTICIPANT.EVENT_ID.in(eventIds))
            .orderBy(PARTICIPANT.EVENT_ID, PARTICIPANT.EMAIL)
            .fetchGroups(PARTICIPANT.EVENT_ID))
        .orElse(Map.of());
  }

  private Optional<Pair<EventRecord, List<ParticipantRecord>>> tryGet(Condition where) {
    val result =
        dsl.select(EVENT.fields())
            .select(PARTICIPANT.fields())
            .from(EVENT)
            .leftJoin(PARTICIPANT).onKey()
            .where(where)
            .collect(
                groupingBy(
                    x -> x.into(EVENT),
                    filtering(x -> x.get(PARTICIPANT.EMAIL) != null,
                        mapping(x -> x.into(PARTICIPANT), toList())
                    )
                )
            );

    val event = result.keySet().stream()
        .findFirst();

    if (event.isEmpty()) {
      return Optional.empty();
    }

    val participants = result.get(event.get());
    return Optional.of(Pair.of(event.get(), participants));
  }

  public Optional<Event> tryGet(EventId id) {
    val result = tryGet(EVENT.ID.eq(id.value()));
    return result.isEmpty()
        ? Optional.empty()
        : Optional.of(eventMapper.toModel(result.get().getLeft(), result.get().getRight()));
  }

  @Override
  public Event get(EventId id) {
    val event = tryGet(EVENT.ID.eq(id.value())).orElseThrow(exceptionHelper::notFound);
    return eventMapper.toModel(event.getLeft(), event.getRight());
  }

  @Override
  public Optional<Event> tryGetByExternalId(EventExternalId externalId) {
    val result = tryGet(EVENT.EXTERNAL_ID.eq(externalId.value()));
    return result.isEmpty()
        ? Optional.empty()
        : Optional.of(eventMapper.toModel(result.get().getLeft(), result.get().getRight()));
  }

  @Override
  public EventAccessInfo getAccessInfo(EventId id) {
    return exceptionHelper.throwNotFoundIfNoData(() -> Fluent.of(dsl
            .select(EVENT.ORG_ID, EVENT.IS_READ_ONLY)
            .from(EVENT)
            .where(EVENT.ID.eq(id.value()))
            .fetchSingle()))
        .map(x -> new EventAccessInfo(new OrgId(x.value1()), x.value2()))
        .get();
  }

  @Override
  public Recurrence getRecurrence(EventId id) {
    return exceptionHelper.throwNotFoundIfNoData(() -> Fluent.of(dsl
            .select(EVENT.RECURRENCE, EVENT.RECURRENCE_MASTER_ID, EVENT.IS_RECURRENCE_OVERRIDE)
            .from(EVENT)
            .where(EVENT.ID.eq(id.value()))
            .fetchSingle()))
        .map(x -> eventMapper.mapToRecurrence(x.value1(), x.value2(), x.value3()))
        .get();
  }

  @Override
  public CoreIds getCoreIds(EventId id) {
    return exceptionHelper.throwNotFoundIfNoData(() -> Fluent.of(dsl
            .select(EVENT.EXTERNAL_ID, EVENT.CALENDAR_ID, EVENT.ORG_ID)
            .from(EVENT)
            .where(EVENT.ID.eq(id.value()))
            .fetchSingle()))
        .map(x -> new CoreIds(
            id,
            Optional.ofNullable(x.value1()).map(EventExternalId::new),
            new CalendarId(x.value2()),
            new OrgId(x.value3())))
        .get();
  }

  @Override
  public CoreIds getCoreIds(EventExternalId externalId) {
    return exceptionHelper.throwNotFoundIfNoData(() -> Fluent.of(dsl
            .select(EVENT.ID, EVENT.CALENDAR_ID, EVENT.ORG_ID)
            .from(EVENT)
            .where(EVENT.EXTERNAL_ID.eq(externalId.value()))
            .fetchSingle()))
        .map(x -> new CoreIds(
            new EventId(x.value1()),
            Optional.of(externalId),
            new CalendarId(x.value2()),
            new OrgId(x.value3())))
        .get();
  }

  @Override
  public Optional<EventId> getId(EventExternalId externalId) {
    return Optional.ofNullable(dsl
            .select(EVENT.ID)
            .from(EVENT)
            .where(EVENT.EXTERNAL_ID.eq(externalId.value()))
            .fetchOne())
        .map(x -> x.value1())
        .map(EventId::new);
  }

  @Override
  public Optional<EventExternalId> getExternalId(EventId id) {
    return Optional.ofNullable(dsl
            .select(EVENT.EXTERNAL_ID)
            .from(EVENT)
            .where(EVENT.ID.eq(id.value()))
            .fetchOne())
        .map(x -> x.value1())
        .map(EventExternalId::new);
  }

  @Override
  public Pair<Optional<AccountId>, Optional<EventExternalId>> getAccountAndExternalIds(EventId id) {
    var record = exceptionHelper.throwNotFoundIfNoData(() -> dsl
        .select(EVENT.calendar().ACCOUNT_ID, EVENT.EXTERNAL_ID)
        .from(EVENT)
        .where(EVENT.ID.eq(id.value()))
        .fetchSingle());
    return Pair.of(
        Optional.ofNullable(record.value1()).map(AccountId::new),
        Optional.ofNullable(record.value2()).map(EventExternalId::new));
  }

  @Override
  public Optional<String> getIcalUid(EventId id) {
    return Optional.ofNullable(dsl
            .select(EVENT.ICAL_UID)
            .from(EVENT)
            .where(EVENT.ID.eq(id.value()))
            .fetchOne())
        .map(x -> x.value1())
        .filter(x -> !x.isBlank());
  }

  @Override
  public void create(EventCreateRequest request) {
    // Create lazy timezone supplier that will only fetch the timezone if needed (all day events).
    val zoneSupplier = createTimeZoneByCalendarIdSupplier(
        Stream.of(request.calendarId()));

    val eventRecord = eventMapper.toRecord(
        request, zoneSupplier.createSupplier(request.calendarId()));
    val participantRecords = participantMapper.toRecordsForCreate(
        request.participants(), request.id());

    dsl.transaction(config -> {
      val txDsl = config.dsl();
      txDsl.executeInsert(eventRecord);

      if (!participantRecords.isEmpty()) {
        txDsl.batchInsert(participantRecords).execute();
      }
    });
  }

  @Override
  public void update(EventUpdateRequest request) {
    if (!request.hasUpdates()) {
      log.debug("Skipping db update of event {} because there are no changes", request.id());
      return;
    }

    // Create lazy timezone supplier that will only fetch the timezone if needed (all day events).
    val zoneSupplier = createTimeZoneByEventIdSupplier(
        Stream.of(request.id()));

    val eventRecord = eventMapper.toRecord(
        request, zoneSupplier.createSupplier(request.id()));

    // If only updating event table, not participants, execute without transaction:
    if (!request.updateFields().contains(EventUpdateRequest.UpdateField.PARTICIPANTS)) {
      exceptionHelper.throwNotFoundIfNoRowsAffected(
          dsl.executeUpdate(eventRecord));
      return;
    }

    dsl.transaction(config -> {
      val txDsl = config.dsl();
      exceptionHelper.throwNotFoundIfNoRowsAffected(
          txDsl.executeUpdate(eventRecord));

      val participants = txDsl
          .selectFrom(PARTICIPANT)
          .where(PARTICIPANT.EVENT_ID.eq(request.id().value()))
          .forNoKeyUpdate()
          .fetchMap(PARTICIPANT.EMAIL);

      val participantsToStore = Optional
          .ofNullable(request.participants())
          .map(list -> list.stream())
          .orElse(Stream.of())
          .map(p -> Optional
              .ofNullable(participants.get(p.email()))
              .map(record -> Fluent
                  .of(record)
                  .also(r -> participantMapper.updateRecord(r, p))
                  .get())
              .orElseGet(() -> participantMapper.toRecordForCreate(p, request.id())))
          .collect(toMap(x -> x.getEmail(), x -> x));

      val participantsToDelete = participants.entrySet().stream()
          .filter(entry -> !participantsToStore.keySet().contains(entry.getKey()))
          .map(entry -> entry.getValue())
          .toList();

      if (!participantsToStore.isEmpty()) {
        txDsl.batchStore(participantsToStore.values()).execute();
      }

      if (!participantsToDelete.isEmpty()) {
        txDsl.batchDelete(participantsToDelete).execute();
      }
    });
  }

  @Override
  public void updateExternalId(EventId id, EventExternalId externalId, DataSource dataSource) {
    // externalId always comes from DataSource.PROVIDER
    updateFields(id, dataSource, (update, now) -> update
        .set(EVENT.EXTERNAL_ID, externalId.value()));
  }

  @Override
  public void updateRecurrenceInstance(
      EventId id, EventExternalId newExternalId, boolean isRecurrenceOverride,
      DataSource dataSource) {
    // newExternalId and recurrence changes always comes from DataSource.PROVIDER
    updateFields(id, dataSource, (update, now) -> update
        .set(EVENT.EXTERNAL_ID, newExternalId.value())
        .set(EVENT.IS_RECURRENCE_OVERRIDE, isRecurrenceOverride));
  }

  @Override
  public void delete(EventId id) {
    dsl.transaction(config -> {
      val txDsl = config.dsl();

      // Delete participants, including for recurrence instances in case the main event is a master.
      txDsl
          .delete(PARTICIPANT)
          .using(EVENT)
          .where(EVENT.ID.eq(PARTICIPANT.EVENT_ID))
          .and(EVENT.ID.eq(id.value())
              .or(EVENT.RECURRENCE_MASTER_ID.eq(id.value())))
          .execute();

      // Delete recurrence instances in case the main event is a master.
      txDsl
          .deleteFrom(EVENT)
          .where(EVENT.RECURRENCE_MASTER_ID.eq(id.value()))
          .execute();

      // Delete the main event.
      exceptionHelper.throwNotFoundIfNoRowsAffected(
          txDsl
              .deleteFrom(EVENT)
              .where(EVENT.ID.eq(id.value()))
              .execute());
    });
  }

  @Override
  public void deleteByExternalId(EventExternalId externalId) {
    // Delete by primary id because we'd have to look it up to handle recurrence series anyway.
    delete(getId(externalId).orElseThrow(exceptionHelper::notFound));
  }

  @Override
  public void tryDeleteByExternalId(EventExternalId externalId) {
    // Delete by primary id because we'd have to look it up to handle recurrence series anyway.
    getId(externalId).ifPresent(id -> {
      try {
        delete(id);
      } catch (NotFoundException ex) {
        Noop.because("We tried to delete and don't care if the event wasn't found.");
      }
    });
  }

  @Override
  public void batchCreate(Collection<EventCreateRequest> requests) {
    if (requests.isEmpty()) {
      return;
    }

    // Create lazy timezone supplier that will only fetch the timezone if needed (all day events).
    // All timezones for the batch will be fetched at once if any of the requests need it.
    val zoneSupplier = createTimeZoneByCalendarIdSupplier(
        requests.stream().map(x -> x.calendarId()));

    val eventInserts = new ArrayList<EventRecord>();
    val recurrenceInstanceInserts = new ArrayList<EventRecord>();
    val participantInserts = new ArrayList<ParticipantRecord>();
    for (var request : requests) {
      val record = eventMapper.toRecord(request, zoneSupplier.createSupplier(request.calendarId()));

      // Gather recurring instance events separately because they may reference a new master event
      // that needs to be inserted first because of the master_event_id FK.
      if (request.recurrence().isInstance()) {
        recurrenceInstanceInserts.add(record);
      } else {
        eventInserts.add(record);
      }

      participantInserts.addAll(participantMapper.toRecordsForCreate(
          request.participants(), request.id()));
    }

    dsl.transaction(config -> {
      val txDsl = config.dsl();
      txDsl.batchInsert(eventInserts).execute();
      txDsl.batchInsert(recurrenceInstanceInserts).execute();
      txDsl.batchInsert(participantInserts).execute();
    });
  }

  @Override
  public void batchUpdate(Collection<EventUpdateRequest> requests) {
    val finalRequests = requests.stream()
        .filter(x -> {
          if (!x.hasUpdates()) {
            log.debug("Skipping db update of event {} because there are no changes", x.id());
          }
          return x.hasUpdates();
        })
        .toList();

    if (finalRequests.isEmpty()) {
      return;
    }

    // Create lazy timezone supplier that will only fetch the timezone if needed (all day events).
    // All timezones for the batch will be fetched at once if any of the requests need it.
    val zoneSupplier = createTimeZoneByEventIdSupplier(
        finalRequests.stream().map(x -> x.id()));

    val eventUpdates = new ArrayList<EventRecord>();
    val participantEventIds = new HashSet<UUID>();

    for (val request : finalRequests) {
      eventUpdates.add(eventMapper.toRecord(request, zoneSupplier.createSupplier(request.id())));

      if (request.updateFields().contains(EventUpdateRequest.UpdateField.PARTICIPANTS)) {
        participantEventIds.add(request.id().value());
      }
    }

    // If only updating event table, not participants, execute without transaction:
    if (participantEventIds.isEmpty()) {
      dsl.batchUpdate(eventUpdates).execute();
      return;
    }

    dsl.transaction(config -> {
      val txDsl = config.dsl();
      txDsl.batchUpdate(eventUpdates).execute();

      val participants = txDsl
          .selectFrom(PARTICIPANT)
          .where(PARTICIPANT.EVENT_ID.in(participantEventIds))
          .forNoKeyUpdate()
          .collect(toMap(x -> Pair.of(x.getEventId(), x.getEmail()), x -> x));

      val participantsToStore = finalRequests.stream()
          .filter(request -> participantEventIds.contains(request.id().value()))
          .flatMap(request -> Optional
              .ofNullable(request.participants())
              .map(list -> list.stream())
              .orElse(Stream.of())
              .map(participant -> Optional
                  .ofNullable(participants.get(Pair.of(request.id().value(), participant.email())))
                  .map(record -> Fluent
                      .of(record)
                      .also(r -> participantMapper.updateRecord(r, participant))
                      .get())
                  .orElseGet(() -> participantMapper.toRecordForCreate(participant, request.id()))))
          .collect(toMap(x -> Pair.of(x.getEventId(), x.getEmail()), x -> x));

      val participantsToDelete = participants.entrySet().stream()
          .filter(entry -> !participantsToStore.keySet().contains(entry.getKey()))
          .map(entry -> entry.getValue())
          .toList();

      if (!participantsToStore.isEmpty()) {
        txDsl.batchStore(participantsToStore.values()).execute();
      }

      if (!participantsToDelete.isEmpty()) {
        txDsl.batchDelete(participantsToDelete).execute();
      }
    });
  }

  @Override
  public void batchDelete(Collection<EventId> ids) {
    if (ids.isEmpty()) {
      return;
    }

    val eventIds = ids.stream().map(x -> x.value()).toList();

    dsl.transaction(config -> {
      val txDsl = config.dsl();

      // Delete participants, including for recurrence instances in case the main event is a master.
      txDsl
          .delete(PARTICIPANT)
          .using(EVENT)
          .where(EVENT.ID.eq(PARTICIPANT.EVENT_ID))
          .and(EVENT.ID.in(eventIds)
              .or(EVENT.RECURRENCE_MASTER_ID.in(eventIds)))
          .execute();

      // Delete recurrence instances.
      txDsl
          .deleteFrom(EVENT)
          .where(EVENT.RECURRENCE_MASTER_ID.in(eventIds))
          .execute();

      // Delete main events.
      txDsl
          .deleteFrom(EVENT)
          .where(EVENT.ID.in(eventIds))
          .execute();
    });
  }

  @Override
  public Optional<AccountId> getAccountId(EventId id) {
    return Optional.ofNullable(
            dsl.select(CALENDAR.ACCOUNT_ID)
                .from(CALENDAR)
                .join(EVENT).on(EVENT.CALENDAR_ID.eq(CALENDAR.ID))
                .where(EVENT.ID.eq(id.value()))
                .and(CALENDAR.ACCOUNT_ID.isNotNull())
                .fetchOne())
        .map(x -> x.value1())
        .map(AccountId::new);
  }

  private LazyBatchCalendarTimeZoneSupplier<CalendarId> createTimeZoneByCalendarIdSupplier(
      Stream<CalendarId> calendarIds) {
    return new LazyBatchCalendarTimeZoneSupplier<>(() -> dsl
        .select(CALENDAR.ID, CALENDAR.TIMEZONE)
        .from(CALENDAR)
        .where(CALENDAR.ID.in(calendarIds.map(x -> x.value()).collect(toSet())))
        .fetchMap(x -> new CalendarId(x.value1()), x -> x.value2()));
  }

  @Override
  public void checkin(EventId id, DataSource dataSource) {
    updateFields(id, dataSource, (update, now) -> update.set(EVENT.CHECKIN_AT, now));
  }

  @Override
  public void checkout(EventId id, DataSource dataSource) {
    updateFields(id, dataSource, (update, now) -> update.set(EVENT.CHECKOUT_AT, now));
  }

  /**
   * Updates UPDATED_AT and UPDATED_FROM plus any additional fields via passed setter.
   */
  private void updateFields(
      EventId id,
      DataSource dataSource,
      BiFunction<
          UpdateSetMoreStep<EventRecord>,
          OffsetDateTime,
          UpdateSetMoreStep<EventRecord>> setter) {

    val now = OffsetDateTime.now();
    val query = Fluent
        .of(dsl
            .update(EVENT)
            .set(EVENT.UPDATED_AT, now)
            .set(EVENT.UPDATED_FROM, dataSource == null ? null : dataSource.value()))
        .map(x -> setter.apply(x, now))
        .map(x -> x.where(EVENT.ID.eq(id.value())))
        .get();

    exceptionHelper.throwNotFoundIfNoRowsAffected(query.execute());
  }

  private LazyBatchCalendarTimeZoneSupplier<EventId> createTimeZoneByEventIdSupplier(
      Stream<EventId> eventIds) {
    return new LazyBatchCalendarTimeZoneSupplier<>(() -> dsl
        .select(EVENT.ID, CALENDAR.TIMEZONE)
        .from(EVENT)
        .join(CALENDAR).on(CALENDAR.ID.eq(EVENT.CALENDAR_ID))
        .where(EVENT.ID.in(eventIds.map(x -> x.value()).collect(toSet())))
        .fetchMap(x -> new EventId(x.value1()), x -> x.value2()));
  }
}
