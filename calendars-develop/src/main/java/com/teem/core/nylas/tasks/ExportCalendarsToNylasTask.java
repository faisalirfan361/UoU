package com.UoU.core.nylas.tasks;

import com.nylas.RequestFailedException;
import com.UoU.core.SecretString;
import com.UoU.core.Task;
import com.UoU.core.accounts.Account;
import com.UoU.core.accounts.AccountCreateRequest;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.AccountRepository;
import com.UoU.core.accounts.AccountUpdateRequest;
import com.UoU.core.accounts.SyncState;
import com.UoU.core.auth.AuthMethod;
import com.UoU.core.calendars.Calendar;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.calendars.CalendarRepository;
import com.UoU.core.calendars.InternalCalendarsConfig;
import com.UoU.core.exceptions.IllegalOperationException;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.nylas.InboundSyncLocker;
import com.UoU.core.nylas.NylasClientFactory;
import com.UoU.core.nylas.auth.NylasAuthService;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * Outbound: Exports calendars to Nylas by id.
 *
 * <p>Currently, this only supports internal calendars.
 */
@Service
@AllArgsConstructor
@Slf4j
public class ExportCalendarsToNylasTask implements Task<ExportCalendarsToNylasTask.Params> {
  public static final Duration INBOUND_SYNC_LOCK_TTL = Duration.ofSeconds(10);

  private final NylasClientFactory nylasClientFactory;
  private final AccountRepository accountRepo;
  private final CalendarRepository calendarRepo;
  private final NylasTaskScheduler scheduler;
  private final NylasAuthService nylasAuthService;
  private final InboundSyncLocker inboundSyncLocker;
  private final InternalCalendarsConfig internalCalendarsConfig;

  public record Params(
      @NonNull Collection<CalendarId> ids,
      boolean includeEvents
  ) {
  }

  @SneakyThrows
  @Override
  public void run(Params params) {
    if (params.ids.isEmpty()) {
      throw new IllegalArgumentException("Missing calendar ids");
    }

    // Schedule separate tasks for each calendar until we're only processing one calendar per task.
    if (params.ids().size() > 1) {
      params.ids().forEach(
          id -> scheduler.exportCalendarsToNylas(List.of(id), params.includeEvents()));
      return;
    }

    // Ids list will now contain a single calendar id to export.
    // This must be an internal calendar since that's all we support for export currently.
    val id = params.ids().stream().findFirst().orElseThrow();
    val calendar = calendarRepo.get(id);

    // Account may already exist if this is a re-export of an existing internal calendar, or if this
    // task ran, failed, and is being retried. If account exists, make sure it's an internal one.
    var existingAccount = Optional.ofNullable(calendar.accountId()).map(accountRepo::get);
    if (existingAccount.filter(x -> x.authMethod() != AuthMethod.INTERNAL).isPresent()) {
      throw new IllegalOperationException("Only internal calendars can be exported.");
    }

    // Derive account name and email from calendar.
    // The account name and email get used as the owner for events created on the virtual calendar.
    // DO-LATER: Nylas doesn't currently support changing the account name, so it could become out
    // of date. Bring this up with Nylas and see if they'll fix, because it makes the most sense
    // to keep the account name and calendar name matching so the event owner is the calendar name.
    val accountName = calendar.name();
    val email = internalCalendarsConfig.getEmail(id);

    // Auth the account in Nylas. If the account already exists, a new access token will be issued.
    val authResult = nylasAuthService.authVirtualAccount(
        accountName,
        email,
        calendar.timezone());
    val accountId = authResult.accountId();

    // If the calendar's account already existed, make sure the new auth result matches or else
    // somehow the calendar/account are in an invalid state.
    if (existingAccount
        .filter(x -> !x.id().equals(accountId))
        .isPresent()) {
      throw new IllegalStateException(
          "Calendar %s is associated with invalid account %s".formatted(
              calendar.id().value(), existingAccount.orElseThrow().id().value()));
    }

    // Create new local account if needed.
    if (existingAccount.isEmpty()) {
      try {
        accountRepo.create(AccountCreateRequest.builder()
            .id(accountId)
            .orgId(calendar.orgId())
            .name(accountName)
            .email(email)
            .authMethod(AuthMethod.INTERNAL)
            .accessToken(authResult.accessToken())
            .syncState(SyncState.RUNNING) // after auth, virtual accounts are immediately running
            .build());
      } catch (DuplicateKeyException ex) {
        // Account already exists, but isn't linked to our calendar, so this task probably already
        // ran and failed. We can link the existing account to the calendar if:
        // - Account id, org, and email match what we tried to create.
        // - Account is an internal account.
        // - Account does not have any other calendars linked to it.
        existingAccount = tryGetAccount(accountId).filter(x ->
            x.orgId().equals(calendar.orgId())
                && x.email().equals(email)
                && x.authMethod() == AuthMethod.INTERNAL
                && calendarRepo.listByAccount(x.orgId(), x.id(), true).findAny().isEmpty());
        existingAccount.orElseThrow(() -> new IllegalStateException(
            "Account %s (%s) already exists but cannot be linked to calendar %s".formatted(
                accountId.value(), email, calendar.id().value()), ex));
      }
    }

    // Update existing local account if needed.
    existingAccount.ifPresent(x -> accountRepo.update(AccountUpdateRequest.builder()
        .id(x.id())
        .name(accountName)
        .accessToken(authResult.accessToken())
        .syncState(SyncState.RUNNING) // after re-auth, virtual accounts are immediately running
        .build()));

    // Create inbound sync lock if we need to create a new Nylas virtual calendar since Nylas will
    // send a calendar.created webhook immediately, and it could be processed before we even finish
    // up here, which could cause some sync tasks to fail. Since we're setting up the new calendar
    // here, any webhooks we receive right after creation should be safe to ignore.
    val inboundSyncLock = calendar.externalId() == null
        ? tryLockInboundSync(accountId)
        : Optional.<UUID>empty();

    // Create or update the Nylas calendar.
    val externalId = createOrUpdateNylasVirtualCalendar(authResult.accessToken(), calendar, email);

    // Link account and external id if either is missing on local calendar.
    if (calendar.accountId() == null || calendar.externalId() == null) {
      calendarRepo.link(id, accountId, externalId);
    }

    if (params.includeEvents()) {
      inboundSyncLock.ifPresentOrElse(
          lock -> scheduler.syncAllEvents(accountId, id, false, lock),
          () -> scheduler.syncAllEvents(accountId, id));
    }

    log.debug("Exported internal calendar to Nylas: {}, {}", id, authResult.accountId());
  }

  private Optional<Account> tryGetAccount(AccountId accountId) {
    try {
      return Optional.of(accountRepo.get(accountId));
    } catch (NotFoundException notFoundEx) {
      return Optional.empty();
    }
  }

  private Optional<UUID> tryLockInboundSync(AccountId accountId) {
    val lock = UUID.randomUUID();
    if (inboundSyncLocker.lockAccount(accountId, INBOUND_SYNC_LOCK_TTL, lock)) {
      return Optional.of(lock);
    }

    log.debug("Inbound sync lock could not be obtained for account: {}", accountId);
    return Optional.empty();
  }

  @SneakyThrows
  private CalendarExternalId createOrUpdateNylasVirtualCalendar(
      SecretString accessToken, Calendar calendar, String email) {

    val client = nylasClientFactory.createAccountClient(accessToken);

    // Update existing calendar.
    if (calendar.externalId() != null) {
      val nylasCalendar = client.calendars().get(calendar.externalId().value());
      setNylasCalendarFields(nylasCalendar, calendar, email);
      client.calendars().update(nylasCalendar);
      log.debug("Updated Nylas virtual calendar: {}", nylasCalendar.getId());
      return calendar.externalId();
    }

    // Create new calendar.
    var nylasCalendar = new com.nylas.Calendar();
    setNylasCalendarFields(nylasCalendar, calendar, email);

    try {
      nylasCalendar = client.calendars().create(nylasCalendar);
      log.debug("Created Nylas virtual calendar: {}", nylasCalendar.getId());
    } catch (RequestFailedException ex) {
      // If 422, calendar probably already exists from a previous, partial failure of this task.
      // Try to fetch the existing calendar for update, else throw original exception.
      // Virtual accounts can only have one calendar, so we can just fetch the first one, if any.
      if (Exceptions.isUnprocessableEntity(ex)) {
        nylasCalendar = client.calendars().list().fetchAll().stream().findFirst()
            .orElseThrow(() -> ex);
        setNylasCalendarFields(nylasCalendar, calendar, email);
        client.calendars().update(nylasCalendar);
        log.debug("Updated Nylas virtual calendar: {}", nylasCalendar.getId());
      } else {
        throw ex;
      }
    }

    return new CalendarExternalId(nylasCalendar.getId());
  }

  private void setNylasCalendarFields(
      com.nylas.Calendar nylasCalendar, Calendar calendar, String email) {

    nylasCalendar.setName(calendar.name());
    nylasCalendar.setTimezone(calendar.timezone());
    // These values aren't used for anything currently but may help when debugging:
    nylasCalendar.setDescription("Internal calendar for " + email);
    nylasCalendar.setMetadata(Map.of("internal", "true", "email", email));
  }
}
