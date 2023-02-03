package com.UoU._integration.db;

import static com.UoU._helpers.PagingAssertions.assertPagesContainValues;
import static com.UoU.core.DataConfig.Calendars.DEFAULT_TIMEZONE;
import static com.UoU.infra.jooq.Tables.CALENDAR;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.UoU._helpers.TestData;
import com.UoU._integration.BaseAppIntegrationTest;
import com.UoU.core.PageParams;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.calendars.CalendarCreateRequest;
import com.UoU.core.calendars.CalendarId;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;

public class JooqCalendarRepositoryTests extends BaseAppIntegrationTest {

  @Test
  void listByAccount_shouldPageAndSortByCreatedAtAndId() {
    val accountId = dbHelper.createAccount(orgId);
    val now = OffsetDateTime.now();

    // Create 4 calendars in the order we expect them returned: by createdAt, id
    val ids = List.of(
        createCalendar(accountId, now, "z"),
        createCalendar(accountId, now.plusSeconds(5), "a"),
        createCalendar(accountId, now.plusSeconds(5), "b"), // same time as prev
        createCalendar(accountId, now.plusSeconds(10), "x"));

    // Get the 4 calendars @ 3 per page
    val limit = 3;
    val repo = dbHelper.getCalendarRepo();
    val page1 = repo.listByAccount(
        orgId, accountId, false, new PageParams(null, limit));
    val page2 = repo.listByAccount(
        orgId, accountId, false, new PageParams(page1.nextCursor(), limit));

    assertPagesContainValues(
        x -> x.id(),
        Pair.of(page1, List.of(ids.get(0), ids.get(1), ids.get(2))),
        Pair.of(page2, List.of(ids.get(3))));
  }

  private CalendarId createCalendar(
      AccountId accountId, OffsetDateTime createdAt, String idStartingWith) {

    val id = new CalendarId(CalendarId.create().value().replaceFirst(".", idStartingWith));
    dbHelper.getDsl()
        .newRecord(CALENDAR)
        .setId(id.value())
        .setAccountId(accountId.value())
        .setName(id.value())
        .setOrgId(orgId.value())
        .setIsReadOnly(false)
        .setTimezone("America/Denver")
        .setCreatedAt(createdAt)
        .insert();

    return id;
  }

  @Test
  void create_shouldUseDefaultTimezoneWhenNull() {
    val id = CalendarId.create();
    dbHelper.getCalendarRepo().create(CalendarCreateRequest.builder()
        .id(id)
        .orgId(TestData.orgId())
        .name("test")
        .isReadOnly(true)
        .build());

    val result = dbHelper.getCalendar(id);

    assertThat(result.getTimezone()).isEqualTo(DEFAULT_TIMEZONE.getId());
  }

  @Test
  void listSyncableCalendarsAtLocalHour_shouldWork() {
    // Delete any calendars left from previous tests because we need to know the exact results.
    dbHelper.resetCalendars();

    // Ensure the zone we use is NOT the default offset because if the default zone is the same one
    // we choose, we can't test the default behavior.
    val rome = ZoneId.of("Europe/Rome");
    val zone = OffsetTime.now(DEFAULT_TIMEZONE).getHour() != OffsetTime.now(rome).getHour()
        ? rome
        : ZoneId.of("America/Chicago");

    val accountId = dbHelper.createAccount(orgId);

    // Create calendars in the zone.
    val calendarIds = Stream
        .generate(() -> dbHelper.createCalendar(orgId, x -> x
            .accountId(accountId)
            .externalId(TestData.calendarExternalId())
            .timezone(zone.getId())
            .isReadOnly(false)))
        .limit(3)
        .collect(Collectors.toSet());

    // Create calendars with null timezone so they use the default.
    val nullTzCalendarIds = Stream
        .generate(() -> dbHelper.createCalendar(orgId, x -> x
            .accountId(accountId)
            .externalId(TestData.calendarExternalId())
            .timezone(null)
            .isReadOnly(false)))
        .limit(2)
        .collect(Collectors.toSet());

    // Create calendars that are ineligible for sync and so should be ignored.
    dbHelper.createCalendar(orgId, x -> x
        .accountId(null) // missing account id, can't sync
        .externalId(TestData.calendarExternalId())
        .isReadOnly(false));
    dbHelper.createCalendar(orgId, x -> x
        .accountId(accountId)
        .externalId(null) // missing external id, can't sync
        .isReadOnly(false));
    dbHelper.createCalendar(orgId, x -> x
        .accountId(accountId)
        .externalId(TestData.calendarExternalId())
        .isReadOnly(true)); // read-only can't sync

    // Check only calendars with our zone offset are returned for the current zone hour.
    val zoneHour = OffsetTime.now(zone).getHour();
    val zoneBatches =
        dbHelper.getCalendarRepo().listSyncableCalendarsAtLocalHour(zoneHour, 2).toList();
    assertThat(zoneBatches.size()).isEqualTo(2);
    AssertionsForClassTypes.assertThat(zoneBatches.stream().flatMap(Collection::stream).count())
        .isEqualTo(calendarIds.size());
    assertThat(zoneBatches).allSatisfy(batch -> assertThat(batch).allSatisfy(x -> {
      assertThat(x.getLeft()).isEqualTo(accountId);
      assertThat(x.getRight()).isIn(calendarIds);
    }));

    // Check that calendars with null timezone are returned for the default zone hour.
    val defaultHour = OffsetTime.now(DEFAULT_TIMEZONE).getHour();
    val defaultBatches =
        dbHelper.getCalendarRepo().listSyncableCalendarsAtLocalHour(defaultHour, 2).toList();
    assertThat(defaultBatches.size()).isEqualTo(1);
    assertThat(defaultBatches.stream().flatMap(Collection::stream).count())
        .isEqualTo(nullTzCalendarIds.size());
    assertThat(defaultBatches).allSatisfy(batch -> assertThat(batch).allSatisfy(x -> {
      assertThat(x.getLeft()).isEqualTo(accountId);
      assertThat(x.getRight()).isIn(nullTzCalendarIds);
    }));
  }
}
