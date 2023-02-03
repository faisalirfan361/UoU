package com.UoU.core.admin;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nylas.RequestFailedException;
import com.UoU._fakes.nylas.NylasMockFactory;
import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU.core.OrgId;
import com.UoU.core.accounts.AccountAccessInfo;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.AccountRepository;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.calendars.CalendarRepository;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.nylas.NylasClientFactory;
import com.UoU.core.nylas.tasks.NylasTaskScheduler;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

class AdminSyncServiceTests {

  @Test
  void syncAllCalendars_shouldScheduleImportWithEvents() {
    val scenario = new Scenario().withAccount();
    val accountId = scenario.accountId.orElseThrow();

    scenario.service.syncAllCalendars(scenario.admin, accountId);

    verify(scenario.deps.nylasTaskSchedulerMock).importAllCalendarsFromNylas(accountId, true);
  }

  @Test
  void syncAllCalendars_shouldThrowForAccountInDifferentOrg() {
    val scenario = new Scenario().withAccount();
    val adminInDifferentOrg = new Admin(TestData.orgId(), "admin");

    assertThatCode(() -> scenario.service.syncAllCalendars(
        adminInDifferentOrg, scenario.accountId.orElseThrow()))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Account");
  }

  @Test
  void syncCalendar_shouldImportWithEvents() {
    val scenario = new Scenario().withAccount().withAccountAndCalendar();
    val accountId = scenario.accountId.orElseThrow();
    val calendarId = scenario.calendarId.orElseThrow();
    val calendarExternalId = scenario.calendarExternalId.orElseThrow();

    scenario.service.syncCalendar(scenario.admin, calendarId);

    verify(scenario.deps.nylasTaskSchedulerMock)
        .importCalendarFromNylas(accountId, calendarExternalId, true);
  }

  @Test
  void syncCalendar_shouldThrowForCalendarInDifferentOrg() {
    val scenario = new Scenario().withAccountAndCalendar();
    val adminInDifferentOrg = new Admin(TestData.orgId(), "admin");

    assertThatCode(() -> scenario.service.syncCalendar(
        adminInDifferentOrg, scenario.calendarId.orElseThrow()))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Calendar");
  }

  @Test
  void updateAccountSyncState_shouldWork() {
    val scenario = new Scenario().withAccount();

    assertThatCode(() -> scenario.service.updateAccountSyncState(
        scenario.admin, scenario.accountId.orElseThrow()))
        .doesNotThrowAnyException();
  }

  @Test
  @SneakyThrows
  void updateAccountSyncState_shouldThrowForCalendarInDifferentOrg() {
    val scenario = new Scenario().withAccount();
    val adminInDifferentOrg = new Admin(TestData.orgId(), "admin");

    assertThatCode(() -> scenario.service.updateAccountSyncState(
        adminInDifferentOrg, scenario.accountId.orElseThrow()))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Account");
  }

  @Test
  void restartAccount_shouldWork() {
    val scenario = new Scenario().withAccount();

    assertThatCode(() -> scenario.service.restartAccount(
        scenario.admin, scenario.accountId.orElseThrow()))
        .doesNotThrowAnyException();
  }

  @Test
  @SneakyThrows
  void restartAccount_shouldThrowAdminOperationExceptionOnDowngradeRequestFailedException() {
    val scenario = new Scenario().withAccount();
    val accountsMock = scenario.deps.nylasClientFactoryMock.createApplicationClient().accounts();

    doThrow(new RequestFailedException(500, "message", "type"))
        .when(accountsMock)
        .downgrade(scenario.accountId.orElseThrow().value());

    assertThatCode(() -> scenario.service.restartAccount(
        scenario.admin, scenario.accountId.orElseThrow()))
        .isInstanceOf(AdminOperationException.class)
        .hasMessageContaining("downgrade");
  }

  @Test
  @SneakyThrows
  void restartAccount_shouldThrowAdminOperationExceptionOnUpgradeRequestFailedException() {
    val scenario = new Scenario().withAccount();
    val accountsMock = scenario.deps.nylasClientFactoryMock.createApplicationClient().accounts();

    doThrow(new RequestFailedException(500, "message", "type"))
        .when(accountsMock)
        .upgrade(scenario.accountId.orElseThrow().value());

    assertThatCode(() -> scenario.service.restartAccount(
        scenario.admin, scenario.accountId.orElseThrow()))
        .isInstanceOf(AdminOperationException.class)
        .hasMessageContaining("upgrade");
  }

  @Test
  void restartAccount_shouldThrowForAccountInDifferentOrg() {
    val scenario = new Scenario().withAccount();
    val adminInDifferentOrg = new Admin(TestData.orgId(), "admin");

    assertThatCode(() -> scenario.service.restartAccount(
        adminInDifferentOrg, scenario.accountId.orElseThrow()))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Account");
  }

  private static class Scenario {
    private OrgId orgId = TestData.orgId();
    private Admin admin = new Admin(orgId, "test");
    private Optional<AccountId> accountId = Optional.empty();
    private Optional<CalendarId> calendarId = Optional.empty();
    private Optional<CalendarExternalId> calendarExternalId = Optional.empty();
    private Dependencies deps = new Dependencies(
        mock(AccountRepository.class),
        mock(CalendarRepository.class),
        mock(NylasTaskScheduler.class),
        NylasMockFactory.createClientFactoryMock(
            NylasMockFactory.createAccountClientMock(),
            NylasMockFactory.createApplicationClient()));
    private AdminSyncService service = new AdminSyncService(
        deps.accountRepoMock,
        deps.calendarRepoMock,
        deps.nylasTaskSchedulerMock,
        deps.nylasClientFactoryMock);

    public Scenario withAccount() {
      accountId = Optional.of(TestData.accountId());
      when(deps.accountRepoMock.getAccessInfo(accountId.orElseThrow()))
          .thenReturn(new AccountAccessInfo(orgId));
      return this;
    }

    public Scenario withAccountAndCalendar() {
      withAccount();
      calendarId = Optional.of(CalendarId.create());
      calendarExternalId = Optional.of(TestData.calendarExternalId());

      when(deps.calendarRepoMock.get(calendarId.orElseThrow()))
          .thenReturn(ModelBuilders.calendarWithTestData()
              .id(calendarId.orElseThrow())
              .externalId(calendarExternalId.orElseThrow())
              .orgId(orgId)
              .accountId(accountId.orElseThrow())
              .isReadOnly(false)
              .build());
      when(deps.calendarRepoMock.getAccountId(calendarId.orElseThrow()))
          .thenReturn(accountId);
      when(deps.calendarRepoMock.tryGetExternalId(calendarId.orElseThrow()))
          .thenReturn(calendarExternalId);

      return this;
    }

    private record Dependencies(
        AccountRepository accountRepoMock,
        CalendarRepository calendarRepoMock,
        NylasTaskScheduler nylasTaskSchedulerMock,
        NylasClientFactory nylasClientFactoryMock) {
    }
  }
}
