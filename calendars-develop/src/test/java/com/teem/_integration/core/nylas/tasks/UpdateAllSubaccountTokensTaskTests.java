package com.UoU._integration.core.nylas.tasks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.UoU.core.accounts.AccountId;
import lombok.val;
import org.junit.jupiter.api.Test;

public class UpdateAllSubaccountTokensTaskTests extends BaseNylasTaskTest {

  @Test
  public void shouldCallUpdateSubaccountTokenForEachSubaccount() {
    val serviceAccountId = dbHelper.createServiceAccount(orgId);
    val accountId1 = dbHelper.createSubaccount(orgId, serviceAccountId);
    val accountId2 = dbHelper.createSubaccount(orgId, serviceAccountId);

    // We don't need to test the inner logic on each iteration, since it's tested separately.
    doNothing()
        .when(getNylasTaskRunnerSpy())
        .updateSubaccountToken(eq(serviceAccountId), any(AccountId.class));

    getNylasTaskRunnerSpy().updateAllSubaccountTokens(serviceAccountId);

    verify(getNylasTaskRunnerSpy()).updateAllSubaccountTokens(serviceAccountId);
    verify(getNylasTaskRunnerSpy()).updateSubaccountToken(serviceAccountId, accountId1);
    verify(getNylasTaskRunnerSpy()).updateSubaccountToken(serviceAccountId, accountId2);
    verifyNoMoreInteractions(getNylasTaskRunnerSpy());
  }
}
