package com.UoU._integration.core.nylas.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import com.UoU._fakes.nylas.FakeNylasAuthService;
import com.UoU._helpers.TestData;
import com.UoU.core.SecretString;
import com.UoU.core.accounts.AccountError;
import lombok.val;
import org.junit.jupiter.api.Test;

public class UpdateSubaccountTokenTaskTests extends BaseNylasTaskTest {

  @Test
  public void updateSubaccountToken_shouldWork() {
    val serviceAccountId = dbHelper.createServiceAccount(orgId);
    val accountEmail = TestData.email();
    val token = new SecretString("token");
    val accountId = dbHelper.createAccount(orgId, x -> x
        .serviceAccountId(serviceAccountId)
        .email(accountEmail)
        .accessToken(token));

    FakeNylasAuthService.fakeAccountIdForEmail(accountEmail, accountId);

    getNylasTaskRunnerSpy().updateSubaccountToken(serviceAccountId, accountId);

    assertThat(dbHelper.getAccountRepo().getAccessToken(accountId).value())
        .isNotEqualTo(token.value());
  }

  @Test
  public void updateSubaccountToken_shouldSaveAccountErrorForNylasException() {
    val serviceAccountId = dbHelper.createServiceAccount(orgId);
    val accountEmail = TestData.email();
    val accountId = dbHelper.createAccount(orgId, x -> x
        .serviceAccountId(serviceAccountId)
        .email(accountEmail));

    val exception = new RuntimeException("Nylas failed " + TestData.uuidString());
    FakeNylasAuthService.fakeExceptionForEmail(accountEmail, exception);

    assertThatCode(() -> getNylasTaskRunnerSpy().updateSubaccountToken(serviceAccountId, accountId))
        .isSameAs(exception);

    val error = dbHelper.getAccountRepo().listErrors(accountId, true)
        .findFirst()
        .orElseThrow();
    assertThat(error.type()).isEqualTo(AccountError.Type.AUTH);
    assertThat(error.details()).contains(exception.getMessage());
  }
}
