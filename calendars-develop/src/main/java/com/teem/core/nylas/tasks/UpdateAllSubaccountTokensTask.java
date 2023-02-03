package com.UoU.core.nylas.tasks;

import com.UoU.core.Task;
import com.UoU.core.accounts.AccountRepository;
import com.UoU.core.accounts.ServiceAccountId;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Batch version of {@link UpdateSubaccountTokenTask} for an entire service account.
 */
@Service
@AllArgsConstructor
public class UpdateAllSubaccountTokensTask
    implements Task<UpdateAllSubaccountTokensTask.Params> {

  NylasTaskScheduler nylasTaskScheduler;
  AccountRepository accountRepo;

  public record Params(@NonNull ServiceAccountId serviceAccountId) {
  }

  @Override
  public void run(Params params) {
    val accounts = accountRepo.listByServiceAccount(params.serviceAccountId());
    accounts.forEach(x ->
        nylasTaskScheduler.updateSubaccountToken(params.serviceAccountId(), x.id())
    );
  }
}
