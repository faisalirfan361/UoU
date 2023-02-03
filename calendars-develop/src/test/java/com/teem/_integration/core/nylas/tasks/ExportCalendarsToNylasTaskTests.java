package com.UoU._integration.core.nylas.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nylas.RequestFailedException;
import com.UoU._fakes.nylas.FakeNylasAuthService;
import com.UoU._fakes.nylas.NylasMockFactory;
import com.UoU._helpers.TestData;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.auth.AuthMethod;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.exceptions.IllegalOperationException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

class ExportCalendarsToNylasTaskTests extends BaseNylasTaskTest {

  @Test
  @SneakyThrows
  @SuppressWarnings("unchecked")
  void shouldScheduleCalendarIdsSeparately() {
    val calendarIds = Stream.generate(CalendarId::create).limit(3).toList();

    // Mock task to collect id when list has single id, else run real task.
    val processedCalendarIds = new ArrayList<CalendarId>();
    doAnswer(inv -> {
      processedCalendarIds.add(((List<CalendarId>) inv.getArgument(0)).get(0));
      return null;
    }).when(getNylasTaskRunnerSpy()).exportCalendarsToNylas(argThat(x -> x.size() == 1), eq(false));

    getNylasTaskRunnerSpy().exportCalendarsToNylas(calendarIds, false);

    assertThat(processedCalendarIds).containsExactlyElementsOf(calendarIds);
  }

  @Test
  @SneakyThrows
  void shouldCreateNewAccountAndNylasCalendar() {
    val calendarId = dbHelper.createCalendar(orgId);
    val accountId = TestData.accountId();

    fakeNylasAuth(calendarId, accountId);

    val externalId = TestData.calendarExternalId();
    val nylasCalendar = NylasMockFactory.createCalendarMock(externalId, accountId);
    when(getAccountClientMock().calendars().create(any(com.nylas.Calendar.class)))
        .thenReturn(nylasCalendar);

    runTask(calendarId);

    assertLinked(calendarId, externalId, accountId);
  }

  @Test
  @SneakyThrows
  void shouldUseExistingAccountAndNylasCalendar() {
    val accountId = dbHelper.createAccount(orgId, x -> x.authMethod(AuthMethod.INTERNAL));
    val externalId = TestData.calendarExternalId();
    val calendarId = dbHelper.createCalendar(orgId, accountId, externalId);

    fakeNylasAuth(calendarId, accountId);

    val nylasCalendar = NylasMockFactory.createCalendarMock(externalId, accountId);
    when(getAccountClientMock().calendars().get(externalId.value())).thenReturn(nylasCalendar);

    // Call task twice to make sure it can be called repeatedly with same result.
    runTask(calendarId);
    runTask(calendarId);

    assertLinked(calendarId, externalId, accountId);

    verify(getAccountClientMock().calendars(), times(2))
        .update(argThat(x -> x.getId().equals(externalId.value())));
  }

  @Test
  @SneakyThrows
  void shouldUseExistingUnlinkedAccount() {
    // Create local calendar and account that are NOT linked together.
    // The account email must be valid for the internal calendar for the linking to be allowed.
    val calendarId = dbHelper.createCalendar(orgId);
    val originalAccountName = "original account name, to tell if account gets updated";
    val email = getDependencies().getInternalCalendarsConfig().getEmail(calendarId);
    val accountId = dbHelper.createAccount(orgId, x -> x
        .authMethod(AuthMethod.INTERNAL)
        .name(originalAccountName)
        .email(email));

    fakeNylasAuth(calendarId, accountId);

    val externalId = TestData.calendarExternalId();
    val nylasCalendar = NylasMockFactory.createCalendarMock(externalId, accountId);
    when(getAccountClientMock().calendars().create(any(com.nylas.Calendar.class)))
        .thenThrow(new RequestFailedException(422, "message", "type"));
    when(getAccountClientMock().calendars().list().fetchAll())
        .thenReturn(List.of(nylasCalendar));

    runTask(calendarId);

    assertLinked(calendarId, externalId, accountId);

    val account = dbHelper.getAccount(accountId);
    assertThat(account.getName()).isNotEqualTo(originalAccountName);
    assertThat(account.getEmail()).isEqualTo(email);
    assertThat(account.getUpdatedAt()).isCloseToUtcNow(within(5, ChronoUnit.SECONDS));
  }

  @Test
  @SneakyThrows
  void shouldThrowWhenExistingUnlinkedAccountEmailDoesNotMatch() {
    // Create local calendar and account that are NOT linked together.
    // The account email will be invalid for the internal calendar, so the linking will fail.
    val calendarId = dbHelper.createCalendar(orgId);
    val accountId = dbHelper.createAccount(orgId, x -> x
        .authMethod(AuthMethod.INTERNAL)
        .email("not-valid-for-internal-calendar@example.com"));

    fakeNylasAuth(calendarId, accountId);

    assertThatCode(() -> runTask(calendarId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("cannot be linked");
  }

  @Test
  @SneakyThrows
  void shouldUseExistingUnlinkedNylasCalendar() {
    val calendarId = dbHelper.createCalendar(orgId);
    val accountId = TestData.accountId();

    fakeNylasAuth(calendarId, accountId);

    val externalId = TestData.calendarExternalId();
    val nylasCalendar = NylasMockFactory.createCalendarMock(externalId, accountId);

    // Nylas returns a 422 error when you try to create more than one virtual calendar per account.
    // And then in that case, the task fetches the existing calendar via list().
    when(getAccountClientMock().calendars().create(any(com.nylas.Calendar.class)))
        .thenThrow(new RequestFailedException(422, "message", "type"));
    when(getAccountClientMock().calendars().list().fetchAll())
        .thenReturn(List.of(nylasCalendar));

    runTask(calendarId);

    assertLinked(calendarId, externalId, accountId);
  }

  @Test
  void shouldThrowForNonInternalAccount() {
    val accountId = dbHelper.createAccount(orgId, x -> x.authMethod(AuthMethod.GOOGLE_OAUTH));
    val calendarId = dbHelper.createCalendar(orgId, accountId, TestData.calendarExternalId());

    assertThatCode(() -> runTask(calendarId))
        .isInstanceOf(IllegalOperationException.class)
        .hasMessageContaining("internal");
  }

  @Test
  void shouldThrowForExistingAccountWithDifferentIdThanAuthResult() {
    val accountId = dbHelper.createAccount(orgId, x -> x.authMethod(AuthMethod.INTERNAL));
    val calendarId = dbHelper.createCalendar(orgId, accountId, TestData.calendarExternalId());

    fakeNylasAuth(calendarId, TestData.accountId()); // doesn't match existing account

    assertThatCode(() -> runTask(calendarId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("invalid account");
  }

  private void runTask(CalendarId calendarId) {
    getNylasTaskRunnerSpy().exportCalendarsToNylas(List.of(calendarId), false);
  }

  private void fakeNylasAuth(CalendarId calendarId, AccountId accountId) {
    val email = getDependencies().getInternalCalendarsConfig().getEmail(calendarId);
    FakeNylasAuthService.fakeAccountIdForEmail(email, accountId);
  }

  private void assertLinked(CalendarId id, CalendarExternalId externalId, AccountId accountId) {
    val calendar = dbHelper.getCalendar(id);
    assertThat(calendar.getExternalId())
        .as("Calendar should be linked to external calendar " + externalId.value())
        .isEqualTo(externalId.value());
    assertThat(calendar.getAccountId())
        .as("Calendar should be linked to account " + accountId.value())
        .isEqualTo(accountId.value());
  }
}
