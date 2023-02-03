package com.UoU._integration.core.nylas.tasks;

import static com.UoU._fakes.nylas.NylasMockFactory.createCalendarMock;
import static com.UoU._fakes.nylas.NylasMockFactory.createEventMocks;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nylas.EventQuery;
import com.UoU._fakes.nylas.FakeInboundSyncLocker;
import com.UoU._helpers.TestData;
import com.UoU.core.calendars.CalendarId;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

public class ImportCalendarFromNylasTaskTests extends BaseNylasTaskTest {

  @Test
  void shouldAbortIfInboundSyncIsLockedForAccount() {
    val accountId = TestData.accountId();
    FakeInboundSyncLocker.fakeIsAccountLockedResult(accountId, true);
    getNylasTaskRunnerSpy().importCalendarFromNylas(
        accountId, TestData.calendarExternalId(), false);
    verifyNoInteractions(getAccountClientMock());
  }

  @SneakyThrows
  @Test
  void shouldCreateCalendar() {
    val accountId = dbHelper.createAccount(orgId);
    val calendarExternalId = TestData.calendarExternalId();

    val nylasCalendar = createCalendarMock(calendarExternalId, accountId);
    when(getAccountClientMock().calendars().get(any(String.class))).thenReturn(nylasCalendar);

    val nylasEventMocks = createEventMocks(calendarExternalId).limit(3).toList();
    when(getAccountClientMock().events().list(any(EventQuery.class)).fetchAll())
        .thenReturn(nylasEventMocks);

    getNylasTaskRunnerSpy().importCalendarFromNylas(accountId, calendarExternalId, true);

    val calendar = dbHelper.getCalendarByExternalId(calendarExternalId);
    val calendarId = new CalendarId(calendar.getId());

    assertThat(calendar).isNotNull();
    assertThat(calendar.getCreatedAt()).isNotNull();
    assertThat(nylasCalendar.getName()).isEqualTo(calendar.getName());
    assertThat(nylasCalendar.isReadOnly()).isEqualTo(calendar.getIsReadOnly());
    assertThat(accountId.value()).isEqualTo(calendar.getAccountId());
    assertThat(orgId.value()).isEqualTo(calendar.getOrgId());
    assertThat(nylasCalendar.getTimezone()).isEqualTo(calendar.getTimezone());

    val events = dbHelper.getEventRepo().listByCalendar(orgId, calendarId);
    val externalIds = events.map(x -> x.externalId().value()).collect(Collectors.toSet());

    assertThat(externalIds.containsAll(nylasEventMocks.stream()
        .map(x -> x.getId()).collect(Collectors.toSet()))).isTrue();
  }

  @SneakyThrows
  @Test
  void shouldUpdateCalendar() {
    val accountId = dbHelper.createAccount(orgId);
    val calendarExternalId = TestData.calendarExternalId();
    dbHelper.createCalendar(orgId, x -> x
        .externalId(calendarExternalId)
        .accountId(accountId)
        .name("Test Calendar Name")
        .timezone("America/Denver")
        .isReadOnly(true));

    val nylasCalendar = createCalendarMock(calendarExternalId, accountId);
    when(getAccountClientMock().calendars().get(any(String.class))).thenReturn(nylasCalendar);

    getNylasTaskRunnerSpy().importCalendarFromNylas(accountId, calendarExternalId, false);

    val calendar = dbHelper.getCalendarByExternalId(calendarExternalId);
    assertThat(calendar.getUpdatedAt()).isNotNull();
    assertThat(nylasCalendar.getName()).isEqualTo(calendar.getName());
    assertThat(nylasCalendar.isReadOnly()).isEqualTo(calendar.getIsReadOnly());
    assertThat(nylasCalendar.getTimezone()).isEqualTo(calendar.getTimezone());

    // These properties should not be editable.
    assertThat(accountId.value()).isEqualTo(calendar.getAccountId());
    assertThat(orgId.value()).isEqualTo(calendar.getOrgId());
  }
}
