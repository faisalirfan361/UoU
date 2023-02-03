package com.UoU.app.v1;

import com.UoU.app.security.Authorize;
import com.UoU.app.security.PrincipalProvider;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.admin.Admin;
import com.UoU.core.admin.AdminSyncService;
import com.UoU.core.calendars.CalendarId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Authorize.Admin // default, but override on each method for clarity
@RestController
@RequestMapping("/v1/admin")
@AllArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "Advanced operations for admins only")
public class AdminController {
  private final AdminSyncService adminSyncService;
  private final PrincipalProvider principalProvider;

  @Authorize.Admin
  @PutMapping("/sync/accounts/{accountId}")
  @Operation(
      summary = "Sync an account, including all calendars and events",
      description = Authorize.Admin.DESCRIPTION)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void syncAccount(@PathVariable String accountId) {
    // DO-LATER: Throttle the sync
    adminSyncService.syncAllCalendars(getAdmin(), new AccountId(accountId));
  }

  @Authorize.Admin
  @PutMapping("/sync/accounts/{accountId}/sync-state")
  @Operation(
      summary = "Updates the account syncState by fetching it from the external provider",
      description = Authorize.Admin.DESCRIPTION
          + "The syncState should normally be kept up-to-date automatically, but this could be "
          + "useful if some system issue prevented the syncState from being updated.")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateAccountSyncState(@PathVariable String accountId) {
    // DO-LATER: Throttle the sync
    adminSyncService.updateAccountSyncState(getAdmin(), new AccountId(accountId));
  }

  @Authorize.Admin
  @PutMapping("/sync/accounts/{accountId}/restart")
  @Operation(
      summary = "Restarts sync by downgrading and immediately upgrading the account",
      description = Authorize.Admin.DESCRIPTION
          + "Restarting an account can fix some sync issues between Nylas (our 3rd-party sync "
          + "provider) and Microsoft/Google, such as an account that has received "
          + "too many errors from Microsoft/Google and so has stopped syncing to Nylas. Only "
          + "restart an account if it has sync issues that cannot otherwise be resolved.\n\n"
          + "See [Nylas Docs](https://developer.nylas.com/docs/support/troubleshooting/"
          + "enable-stopped-accounts/)")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void restartAccount(@PathVariable String accountId) {
    // DO-LATER: Throttle the operation
    adminSyncService.restartAccount(getAdmin(), new AccountId(accountId));
  }

  @Authorize.Admin
  @PutMapping("/sync/calendars/{calendarId}")
  @Operation(
      summary = "Sync a calendar and all its events",
      description = Authorize.Admin.DESCRIPTION)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void syncCalendar(@PathVariable String calendarId) {
    // DO-LATER: Throttle the sync
    adminSyncService.syncCalendar(getAdmin(), new CalendarId(calendarId));
  }

  private Admin getAdmin() {
    return new Admin(
        principalProvider.current().orgId(),
        principalProvider.current().subject());
  }
}
