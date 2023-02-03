package com.UoU.core.nylas.tasks;

import com.nylas.RequestFailedException;
import com.UoU.core.Task;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.AccountRepository;
import com.UoU.core.accounts.SyncState;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.nylas.NylasClientFactory;
import com.UoU.core.nylas.mapping.NylasAccountMapper;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Inbound: Imports the latest account sync state from Nylas and stores locally.
 *
 * <p>This does NOT use {@link com.UoU.core.nylas.InboundSyncLocker} to prevent updates when the
 * account is locked because sync state updates are relatively rare and unlikely to cause issues.
 * when other sync operations are happening.
 */
@Service
@AllArgsConstructor
@Slf4j
public class UpdateAccountSyncStateTask implements Task<UpdateAccountSyncStateTask.Params> {
  private final AccountRepository accountRepo;
  private final NylasAccountMapper accountMapper;
  private final NylasClientFactory nylasClientFactory;

  public record Params(
      @NonNull AccountId accountId) {
  }

  @Override
  public void run(Params params) {
    val nylasAccount = getNylasAccount(params.accountId());
    val newSyncState = nylasAccount
        .map(accountMapper::toSyncStateModel)
        .orElse(SyncState.UNKNOWN); // clear out sync state if nylas account not found

    SyncState existingSyncState;
    try {
      existingSyncState = accountRepo.get(params.accountId()).syncState();
    } catch (NotFoundException ex) {
      // Occasionally, a new local account may not exist yet because an account.created webhook gets
      // processed before we save the account to our db. This is mainly a risk for virtual accounts
      // because the webhooks are so fast, and in that case the account will be saved as running
      // immediately anyway. If the nylas account exists and is running, just ignore this scenario.
      if (nylasAccount.isPresent() && newSyncState == SyncState.RUNNING) {
        log.debug("Skipping sync state update for RUNNING account not found (yet) locally: {}",
            params.accountId());
        return;
      }

      throw ex;
    }

    if (existingSyncState != newSyncState) {
      accountRepo.updateSyncState(params.accountId(), newSyncState);
      log.debug("Updated sync state for {} from {} to {}",
          params.accountId(), existingSyncState, newSyncState);
    } else {
      log.debug("Skipping sync state update for {} because it hasn't changed: {}",
          params.accountId(), newSyncState);
    }

    // If nylas account was not found, throw not found because something is not right. Even though
    // we already did the update to clear out the existing sync state above, this task should fail.
    if (nylasAccount.isEmpty()) {
      throw new NotFoundException("Nylas account not found: " + params.accountId());
    }

    // DO-LATER: Should sync state transitions such as stopped->running be used to handle cases like
    // where an account's auth is invalid but then gets fixed and we need to do a full sync? For
    // now, we'll just let those things be resolved manually through admin actions, but this may
    // be a future area of research where we can proactively identify and resolve sync issues.
  }

  @SneakyThrows
  private Optional<com.nylas.Account> getNylasAccount(AccountId accountId) {
    val nylas = nylasClientFactory.createApplicationClient();

    try {
      return Optional.of(nylas.accounts().get(accountId.value()));
    } catch (RequestFailedException ex) {
      if (ex.getStatusCode() == 404) {
        return Optional.empty();
      }
      throw ex;
    }
  }
}
