package com.UoU.core.admin;

import com.nylas.RequestFailedException;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.AccountRepository;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.calendars.CalendarRepository;
import com.UoU.core.nylas.NylasClientFactory;
import com.UoU.core.nylas.tasks.NylasTaskScheduler;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Service for doing admin sync things to recover from sync issues and so on.
 */
@Service
@AllArgsConstructor
@Slf4j
public class AdminSyncService {
  private final AccountRepository accountRepo;
  private final CalendarRepository calendarRepo;
  private final NylasTaskScheduler nylasTaskScheduler;
  private final NylasClientFactory nylasClientFactory;

  public void syncAllCalendars(Admin admin, AccountId accountId) {
    accountRepo.getAccessInfo(accountId)
        .requireOrgOrThrowNotFound(admin.orgId());

    logAdminAction(
        admin,
        "syncAllCalendars",
        Map.of("accountId", accountId.value()));

    nylasTaskScheduler.importAllCalendarsFromNylas(accountId, true);
  }

  public void syncCalendar(Admin admin, CalendarId calendarId) {
    val calendar = calendarRepo.get(calendarId);

    calendar.getAccessInfo().requireOrgOrThrowNotFound(admin.orgId());
    calendar.requireIsEligibleToSync();

    logAdminAction(
        admin,
        "syncCalendar",
        Map.of(
            "accountId", calendar.accountId().value(),
            "calendarId", calendarId.value(),
            "calendarExternalId", calendar.externalId().value()));

    nylasTaskScheduler.importCalendarFromNylas(calendar.accountId(), calendar.externalId(), true);
  }

  /**
   * Updates the account sync state by fetching the latest value from Nylas.
   *
   * <p>The sync state should be kept up-to-date automatically, so this is only necessary when
   * an account gets in a weird state from system or webhook issues.
   */
  public void updateAccountSyncState(Admin admin, AccountId accountId) {
    accountRepo.getAccessInfo(accountId)
        .requireOrgOrThrowNotFound(admin.orgId());

    logAdminAction(
        admin,
        "updateAccountSyncState",
        Map.of("accountId", accountId.value()));

    nylasTaskScheduler.updateAccountSyncState(accountId);
  }

  /**
   * Restarts Nylas account sync via downgrade/upgrade as instructed in the Nylas docs.
   *
   * <p>Restarting can help resolve some sync issues between Nylas and external providers. See also:
   * https://developer.nylas.com/docs/support/troubleshooting/enable-stopped-accounts/
   */
  @SneakyThrows
  public void restartAccount(Admin admin, AccountId accountId) {
    accountRepo.getAccessInfo(accountId)
        .requireOrgOrThrowNotFound(admin.orgId());

    val nylas = nylasClientFactory.createApplicationClient();

    logAdminAction(
        admin,
        "restartAccount",
        Map.of("accountId", accountId.value()));

    try {
      nylas.accounts().downgrade(accountId.value());
    } catch (RequestFailedException ex) {
      throw new AdminOperationException(
          "Failed to restart the account (downgrade failed): " + ex, ex);
    }

    try {
      nylas.accounts().upgrade(accountId.value());
    } catch (RequestFailedException ex) {
      throw new AdminOperationException(
          "Failed to restart the account (upgrade failed): " + ex, ex);
    }
  }

  private static void logAdminAction(Admin admin, String action, Map<String, Object> context) {
    if (!log.isInfoEnabled()) {
      return;
    }

    var contextCsv = context
        .entrySet()
        .stream()
        .map(entry -> entry.getKey() + "=" + entry.getValue().toString())
        .collect(Collectors.joining(", "));

    log.info("ADMIN-ACTION [{}]: org={}, admin={}, {}",
        action,
        admin.orgId().value(),
        admin.id(),
        contextCsv);
  }
}
