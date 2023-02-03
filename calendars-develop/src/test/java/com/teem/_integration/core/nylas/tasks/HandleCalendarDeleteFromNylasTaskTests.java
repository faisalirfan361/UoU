package com.UoU._integration.core.nylas.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.UoU._fakes.nylas.FakeInboundSyncLocker;
import com.UoU._helpers.TestData;
import lombok.val;
import org.jooq.exception.NoDataFoundException;
import org.junit.jupiter.api.Test;

public class HandleCalendarDeleteFromNylasTaskTests extends BaseNylasTaskTest {

  @Test
  void shouldAbortIfInboundSyncIsLockedForAccount() {
    val accountId = dbHelper.createAccount(orgId);
    val calendarExternalId = TestData.calendarExternalId();
    val calendarId = dbHelper.createCalendar(orgId, accountId, calendarExternalId);

    FakeInboundSyncLocker.fakeIsAccountLockedResult(accountId, true);
    getNylasTaskRunnerSpy().handleCalendarDeleteFromNylas(accountId, calendarExternalId);

    assertThat(dbHelper.getCalendarRepo().exists(calendarId))
        .as("Task should have aborted, so calendar should still exist.")
        .isTrue();
  }

  @Test
  void shouldWork() {
    val accountId = dbHelper.createAccount(orgId);
    val calendarExternalId = TestData.calendarExternalId();
    val calendarId = dbHelper.createCalendar(orgId, accountId, calendarExternalId);
    val eventId = dbHelper.createEvent(orgId, calendarId);

    getNylasTaskRunnerSpy().handleCalendarDeleteFromNylas(accountId, calendarExternalId);

    assertThrows(NoDataFoundException.class, () -> dbHelper.getEvent(eventId));
    assertThrows(NoDataFoundException.class, () -> dbHelper.getCalendar(calendarId));
  }

  @Test
  void shouldWorkWhenNotFound() {
    val accountId = dbHelper.createAccount(orgId);
    val calendarExternalId = TestData.calendarExternalId();

    assertThatCode(() -> getNylasTaskRunnerSpy()
        .handleCalendarDeleteFromNylas(accountId, calendarExternalId))
        .doesNotThrowAnyException();
  }
}
