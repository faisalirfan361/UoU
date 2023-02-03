package com.UoU._integration.core.nylas.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nylas.Calendar;
import com.UoU._fakes.nylas.FakeInboundSyncLocker;
import com.UoU._fakes.nylas.NylasMockFactory;
import com.UoU._helpers.TestData;
import com.UoU.core.OrgId;
import com.UoU.core.accounts.AccountId;
import com.UoU.infra.jooq.tables.records.CalendarRecord;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ImportAllCalendarsFromNylasTaskTests extends BaseNylasTaskTest {
  private OrgId orgId;
  private AccountId accountId;

  @BeforeEach
  void setup() {
    orgId = TestData.orgId();
    accountId = dbHelper.createAccount(orgId);
  }

  @AfterEach
  void teardown() {
    dependencies.setNylasAccountClientMock(NylasMockFactory.createAccountClientMock());
  }

  @Test
  void shouldAbortIfInboundSyncAccountLockNotObtained() {
    FakeInboundSyncLocker.fakeLockAccountResult(accountId, false);
    getNylasTaskRunnerSpy().importAllCalendarsFromNylas(accountId, false);
    verifyNoInteractions(getAccountClientMock());
  }

  @SneakyThrows
  @Test
  void shouldCreateCalendars() {
    val calendarMocks = NylasMockFactory.createCalendarMocks(accountId).limit(2).toList();
    when(getAccountClientMock().calendars().list().fetchAll()).thenReturn(calendarMocks);

    getNylasTaskRunnerSpy().importAllCalendarsFromNylas(accountId, false);

    val calendars = dbHelper.getCalendars(accountId).toList();
    validateResults(calendarMocks, calendars);
  }

  @SneakyThrows
  @Test
  void shouldUpdateCalendars() {
    val calendarExternalIds = Stream.generate(TestData::calendarExternalId).limit(2).toList();
    dbHelper.createCalendars(orgId, accountId, calendarExternalIds);

    val calendarMocks = NylasMockFactory
        .createCalendarMocks(accountId, calendarExternalIds).toList();
    when(getAccountClientMock().calendars().list().fetchAll()).thenReturn(calendarMocks);

    getNylasTaskRunnerSpy().importAllCalendarsFromNylas(accountId, true);
    val calendars = dbHelper.getCalendars(accountId).toList();

    validateResults(calendarMocks, calendars);
  }

  @Test
  void shouldDeleteCalendarsWithExternalIds() {
    val externalIds = Stream.generate(TestData::calendarExternalId).limit(2).toList();
    dbHelper.createCalendars(orgId, accountId, externalIds);

    // Calendars without external ids should be preserved.
    val calsWithoutExternalIds = dbHelper.createCalendars(orgId, accountId)
        .map(x -> x.value())
        .limit(2)
        .toList();

    getNylasTaskRunnerSpy().importAllCalendarsFromNylas(accountId, false);
    val resultIds = dbHelper.getCalendars(accountId).map(x -> x.getId()).toList();

    assertThat(resultIds).containsExactlyInAnyOrderElementsOf(calsWithoutExternalIds);
  }

  private void validateResults(List<Calendar> calendarMocks, List<CalendarRecord> calendars) {
    assertThat(calendarMocks.size()).isEqualTo(calendars.size());
    calendarMocks.forEach(mock -> calendars.stream()
        .filter(calendar -> calendar.getExternalId().equals(mock.getId()))
        .findFirst()
        .ifPresentOrElse(x -> {
          assertThat(x.getCreatedAt()).isNotNull();
          assertThat(mock.getTimezone()).isEqualTo(x.getTimezone());
          assertThat(mock.getName()).isEqualTo(x.getName());
          assertThat(mock.getAccountId()).isEqualTo(x.getAccountId());
          assertThat(mock.isReadOnly()).isEqualTo(x.getIsReadOnly());
          assertThat(orgId.value()).isEqualTo(x.getOrgId());
        }, () -> fail("Calendar was not found")));
  }
}
