package com.UoU.core.nylas.tasks;

import com.UoU.core.Task;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.auth.AuthService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;

/**
 * Updates a subaccount token by fetching from Nylas and storing locally.
 */
@Service
@AllArgsConstructor
public class UpdateSubaccountTokenTask implements Task<UpdateSubaccountTokenTask.Params> {
  private final AuthService authService;

  public record Params(
      @NonNull ServiceAccountId serviceAccountId,
      @NonNull AccountId accountId
  ) {
  }

  public void run(Params params) {
    // updateSubaccountToken will save any auth error to account errors so the user can see it.
    // serviceAccountId is not currently needed but we'll leave the param in case anything needs
    // it later, like for caching to prevent loading the service account each time.
    authService.updateSubaccountToken(params.accountId());
  }
}
