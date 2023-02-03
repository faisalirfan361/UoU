package com.UoU.core.nylas.tasks;

import com.nylas.RequestFailedException;
import com.UoU.core.Task;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.nylas.NylasClientFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Outbound: Deletes account from Nylas after it's already been deleted locally.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteAccountFromNylasTask implements Task<DeleteAccountFromNylasTask.Params> {
  private final NylasClientFactory nylasClientFactory;

  public record Params(
      @NonNull AccountId accountId
  ) {
  }

  @Override
  @SneakyThrows
  public void run(Params params) {
    val nylas = nylasClientFactory.createApplicationClient();

    try {
      nylas.accounts().delete(params.accountId().value());
      log.debug("Deleted account from Nylas: {}", params.accountId());
    } catch (RequestFailedException ex) {
      if (Exceptions.isNotFound(ex)) {
        // If not found, consider that success so delete task is idempotent.
        log.info("Delete from Nylas failed because account was not found: {}", params.accountId());
      } else {
        throw ex;
      }
    }
  }
}
