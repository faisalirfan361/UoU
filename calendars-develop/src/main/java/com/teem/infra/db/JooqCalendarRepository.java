package com.UoU.infra.db;

import static com.UoU.infra.jooq.Tables.ACCOUNT;
import static com.UoU.infra.jooq.Tables.CALENDAR;
import static com.UoU.infra.jooq.Tables.EVENT;
import static com.UoU.infra.jooq.Tables.PARTICIPANT;

import com.UoU.core.Fluent;
import com.UoU.core.OrgId;
import com.UoU.core.PageParams;
import com.UoU.core.PagedItems;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.Provider;
import com.UoU.core.calendars.Calendar;
import com.UoU.core.calendars.CalendarAccessInfo;
import com.UoU.core.calendars.CalendarCreateRequest;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.calendars.CalendarRepository;
import com.UoU.core.calendars.CalendarUpdateRequest;
import com.UoU.infra.db.mapping.JooqAccountMapper;
import com.UoU.infra.db.mapping.JooqCalendarMapper;
import com.UoU.infra.jooq.tables.records.CalendarRecord;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.SelectConditionStep;
import org.jooq.SelectSeekStep1;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class JooqCalendarRepository implements CalendarRepository {
  private final DSLContext dsl;
  private final JooqAccountMapper accountMapper;
  private final JooqCalendarMapper calendarMapper;
  private final ExceptionHelper exceptionHelper = new ExceptionHelper(Calendar.class);

  @Override
  public Stream<Calendar> listByAccount(
      OrgId orgId, AccountId accountId, boolean includeReadOnly) {

    val records = selectAccounts(orgId, accountId, includeReadOnly)
        .orderBy(CALENDAR.CREATED_AT, CALENDAR.ID)
        .fetch();

    return records.stream().map(calendarMapper::toModel);
  }

  @Override
  public PagedItems<Calendar> listByAccount(
      OrgId orgId, AccountId accountId, boolean includeReadOnly, PageParams page) {

    // Cursor paging fields: CREATED_AT, ID, includeReadOnly (param)
    // includeReadOnly is only used to ensure filter is constant across pages.
    val cursor = Cursor.decoder().decodeThreeAndMap(
        page.cursor(),
        (createdAt, id, readOnly) -> {
          if (includeReadOnly != Boolean.parseBoolean(readOnly)) {
            throw new IllegalArgumentException("Invalid cursor for includeReadOnly");
          }
          return Pair.of(OffsetDateTime.parse(createdAt), id);
        });

    val records = Fluent
        .of(selectAccounts(orgId, accountId, includeReadOnly))
        .ifThenAlso(cursor, (query, cursorValue) -> query
            .and(CALENDAR.CREATED_AT.gt(cursorValue.getLeft())
                .or(CALENDAR.CREATED_AT.eq(cursorValue.getLeft())
                    .and(CALENDAR.ID.gt(cursorValue.getRight())))))
        .get()
        .orderBy(CALENDAR.CREATED_AT, CALENDAR.ID)
        .limit(Math.max(2, page.limit() + 1)) // fetch one extra so we know if there's a next page
        .fetch();

    val nextCursor = Optional
        .of(records)
        .filter(x -> x.size() > page.limit() && x.size() >= 2)
        .map(x -> x.get(x.size() - 2)) // last in page, accounting for one extra
        .map(x -> new Cursor(x.getCreatedAt(), x.getId(), includeReadOnly).encode());
    nextCursor.ifPresent(x -> records.remove(records.size() - 1)); // remove one extra

    return new PagedItems<>(
        records.map(calendarMapper::toModel),
        nextCursor.orElse(null));
  }

  private SelectConditionStep<CalendarRecord> selectAccounts(
      OrgId orgId, AccountId accountId, boolean includeReadOnly) {
    return Fluent.of(dsl
            .selectFrom(CALENDAR)
            .where(Conditions.orgMatches(CALENDAR, orgId))
            .and(CALENDAR.ACCOUNT_ID.eq(accountId.value())))
        .ifThenAlso(!includeReadOnly, x -> x.and(CALENDAR.IS_READ_ONLY.eq(false)))
        .get();
  }

  /**
   * Returns batches of syncable calendars that are currently at the localHour based on timezone.
   *
   * <p>Minutes are ignored, so a calendar would be at hour 0 from 00:00 until 00:59.
   *
   * <p>DataConfig.Calendars.DEFAULT_TIMEZONE will be used for calendars without a timezone.
   */
  @Override
  public Stream<List<Pair<AccountId, CalendarId>>> listSyncableCalendarsAtLocalHour(
      int localHour, int batchSize) {

    if (localHour < 0 || localHour > 23) {
      throw new IllegalArgumentException("Hour of day must be 0-23.");
    }

    if (batchSize <= 0) {
      throw new IllegalArgumentException("Batch size must be greater than 0.");
    }

    val calendar = CALENDAR.as("c");
    final Supplier<SelectSeekStep1<Record2<String, String>, String>> query = () -> dsl
        .select(calendar.ACCOUNT_ID, calendar.ID)
        .from(calendar)
        .where(calendar.ACCOUNT_ID.isNotNull())
        .and(calendar.EXTERNAL_ID.isNotNull())
        .and(calendar.IS_READ_ONLY.eq(false)) // we don't sync read-only calendars
        .and("EXTRACT(HOUR FROM NOW() AT TIME ZONE c.timezone) = ?", localHour)
        .orderBy(calendar.ID);

    // Return a lazy stream of batches by executing the query for each next batch on iteration.
    // This continues while the returned rows >= batch size, which means there could be more.
    // Each batch query is created by using the last calendar id from the prev batch as a cursor.
    return Stream
        .iterate(
            query.get().limit(batchSize).fetch(), // seed
            batch -> batch != null && batch.isNotEmpty(), // has next?
            batch -> batch.size() < batchSize ? null : query.get() // create next
                .seekAfter(batch.get(batch.size() - 1).getValue(calendar.ID))
                .limit(batchSize)
                .fetch())
        .filter(batch -> batch != null && batch.isNotEmpty())
        .map(batch -> batch.map(record -> Pair.of(
            new AccountId(record.getValue(calendar.ACCOUNT_ID)),
            new CalendarId(record.getValue(calendar.ID)))));
  }

  @Override
  public boolean exists(CalendarId id) {
    return dsl.fetchExists(CALENDAR, CALENDAR.ID.eq(id.value()));
  }

  @Override
  public Calendar get(CalendarId id) {
    return tryGet(id).orElseThrow(exceptionHelper::notFound);
  }

  @Override
  public Optional<Calendar> tryGet(CalendarId id) {
    return dsl
        .selectFrom(CALENDAR)
        .where(CALENDAR.ID.eq(id.value()))
        .fetchOptional(calendarMapper::toModel);
  }

  @Override
  public Optional<Calendar> tryGetByExternalId(CalendarExternalId externalId) {
    return dsl
        .selectFrom(CALENDAR)
        .where(CALENDAR.EXTERNAL_ID.eq(externalId.value()))
        .fetchOptional(calendarMapper::toModel);
  }

  @Override
  public Optional<CalendarId> tryGetId(CalendarExternalId externalId) {
    return dsl
        .select(CALENDAR.ID)
        .from(CALENDAR)
        .where(CALENDAR.EXTERNAL_ID.eq(externalId.value()))
        .fetchOptional(x -> x.value1())
        .map(CalendarId::new);
  }

  @Override
  public Optional<CalendarExternalId> tryGetExternalId(CalendarId id) {
    return dsl
        .select(CALENDAR.EXTERNAL_ID)
        .from(CALENDAR)
        .where(CALENDAR.ID.eq(id.value()))
        .fetchOptional(x -> x.value1())
        .map(CalendarExternalId::new);
  }

  @Override
  public Optional<CalendarAccessInfo> tryGetAccessInfo(CalendarId id) {
    return dsl
        .select(CALENDAR.ORG_ID, CALENDAR.IS_READ_ONLY)
        .from(CALENDAR)
        .where(CALENDAR.ID.eq(id.value()))
        .fetchOptional(x -> new CalendarAccessInfo(new OrgId(x.value1()), x.value2()));
  }

  @Override
  public Optional<AccountId> getAccountId(CalendarId id) {
    return Optional.ofNullable(dsl
            .select(CALENDAR.ACCOUNT_ID)
            .from(CALENDAR)
            .where(CALENDAR.ID.eq(id.value()))
            .fetchOne())
        .map(x -> x.value1())
        .map(AccountId::new);
  }

  @Override
  public Optional<Provider> getAccountProvider(CalendarId id) {
    return Optional.ofNullable(dsl
            .select(ACCOUNT.AUTH_METHOD)
            .from(ACCOUNT)
            .join(CALENDAR).on(ACCOUNT.ID.eq(CALENDAR.ACCOUNT_ID))
            .where(CALENDAR.ID.eq(id.value()))
            .fetchOne())
        .map(x -> accountMapper.toModelEnum(x.value1()).getProvider());
  }

  @Override
  public String getTimezone(CalendarId id) {
    return exceptionHelper.throwNotFoundIfNoData(() -> dsl
        .select(CALENDAR.TIMEZONE)
        .from(CALENDAR)
        .where(CALENDAR.ID.eq(id.value()))
        .fetchSingle()
        .value1());
  }

  @Override
  public void create(CalendarCreateRequest request) {
    val record = calendarMapper.toRecord(request);
    dsl.executeInsert(record);
  }

  @Override
  public void update(CalendarUpdateRequest request) {
    if (!request.hasUpdates()) {
      log.debug("Skipping db update of calendar {} because there are no changes", request.id());
      return;
    }

    val record = calendarMapper.toRecord(request);
    exceptionHelper.throwNotFoundIfNoRowsAffected(
        dsl.executeUpdate(record));
  }

  @Override
  public void link(CalendarId id, AccountId accountId, CalendarExternalId externalId) {
    exceptionHelper.throwNotFoundIfNoRowsAffected(dsl
        .update(CALENDAR)
        .set(CALENDAR.ACCOUNT_ID, accountId.value())
        .set(CALENDAR.EXTERNAL_ID, externalId.value())
        .set(CALENDAR.UPDATED_AT, OffsetDateTime.now())
        .where(CALENDAR.ID.eq(id.value()))
        // Ensure that account and external id never change once set:
        .and(CALENDAR.ACCOUNT_ID.isNull().or(CALENDAR.ACCOUNT_ID.eq(accountId.value())))
        .and(CALENDAR.EXTERNAL_ID.isNull().or(CALENDAR.EXTERNAL_ID.eq(externalId.value())))
        .execute());
  }

  @Override
  public void delete(CalendarId id) {
    dsl.transaction(config -> {
      val txDsl = config.dsl();

      txDsl
          .delete(PARTICIPANT)
          .using(EVENT)
          .where(EVENT.CALENDAR_ID.eq(id.value()))
          .execute();

      txDsl
          .deleteFrom(EVENT)
          .where(EVENT.CALENDAR_ID.eq(id.value()))
          .execute();

      exceptionHelper.throwNotFoundIfNoRowsAffected(
          txDsl
              .deleteFrom(CALENDAR)
              .where(CALENDAR.ID.eq(id.value()))
              .execute());
    });
  }

  @Override
  public void batchCreate(List<CalendarCreateRequest> requests) {
    val records = requests.stream()
        .map(calendarMapper::toRecord)
        .toList();

    dsl.batchInsert(records).execute();
  }

  @Override
  public void batchUpdate(List<CalendarUpdateRequest> requests) {
    val records = requests.stream()
        .filter(x -> {
          if (!x.hasUpdates()) {
            log.debug("Skipping db update of calendar {} because there are no changes", x.id());
          }
          return x.hasUpdates();
        })
        .map(calendarMapper::toRecord)
        .toList();

    if (!records.isEmpty()) {
      dsl.batchUpdate(records).execute();
    }
  }

  @Override
  public void batchDelete(List<CalendarId> ids) {
    val calendarRecords = ids.stream().map(x -> new CalendarRecord().setId(x.value())).toList();
    val calendarIds = ids.stream().map(x -> x.value()).toList();

    dsl.transaction(config -> {
      val txDsl = config.dsl();

      txDsl
          .deleteFrom(PARTICIPANT)
          .where(PARTICIPANT.event().calendar().ID.in(ids.stream().map(x -> x.value()).toList()));

      txDsl
          .deleteFrom(EVENT)
          .where(EVENT.CALENDAR_ID.in(calendarIds))
          .execute();

      txDsl
          .batchDelete(calendarRecords)
          .execute();
    });
  }
}
