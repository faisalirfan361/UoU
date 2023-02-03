package com.UoU._integration.core.nylas.tasks;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

import com.nylas.RequestFailedException;
import com.UoU._fakes.nylas.FakeInboundSyncLocker;
import com.UoU._fakes.nylas.NylasMockFactory;
import com.UoU._helpers.TestData;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.SyncState;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.infra.jooq.enums.NylasAccountSyncState;
import java.time.temporal.ChronoUnit;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

public class UpdateAccountSyncStateTaskTests extends BaseNylasTaskTest {
  void runTask(AccountId accountId) {
    getNylasTaskRunnerSpy().updateAccountSyncState(accountId);
  }

  @Test
  @SneakyThrows
  void shouldStillRunIfInboundSyncIsLockedForAccount() {
    val accountId = TestData.accountId();
    FakeInboundSyncLocker.fakeLockAccountResult(accountId, true);

    when(getAppClientMock().accounts().get(accountId.value()))
        .thenThrow(new RequestFailedException(404, "message", "type"));

    // If account not found exception is thrown, task processing got past account lock:
    assertThatCode(() -> runTask(accountId)).isInstanceOf(NotFoundException.class);
  }

  @Test
  @SneakyThrows
  void shouldUpdateSyncStateWhenDifferent() {
    val accountId = dbHelper.createAccount(orgId);
    val accountMock = NylasMockFactory.createAccountMock(accountId, "invalid-credentials");
    when(getAppClientMock().accounts().get(accountId.value())).thenReturn(accountMock);

    // Run task: should update sync state since it starts out as null in db.
    runTask(accountId);
    val account1 = dbHelper.getAccount(accountId);

    assertThat(account1.getNylasSyncState()).isEqualTo(NylasAccountSyncState.invalid_credentials);
    assertThat(account1.getUpdatedAt()).isCloseToUtcNow(within(1, ChronoUnit.SECONDS));

    // Run task again: should NOT update this time since it will be the same as db.
    runTask(accountId);
    val account2 = dbHelper.getAccount(accountId);

    assertThat(account2.getNylasSyncState()).isEqualTo(NylasAccountSyncState.invalid_credentials);
    assertThat(account2.getUpdatedAt()).isEqualTo(account1.getUpdatedAt());
  }

  @Test
  @SneakyThrows
  void shouldSetSyncStateToUnknownOnInvalidNylasValue() {
    val accountId = dbHelper.createAccount(orgId);
    val accountMock = NylasMockFactory.createAccountMock(accountId, "test-invalid-value");
    when(getAppClientMock().accounts().get(accountId.value())).thenReturn(accountMock);

    runTask(accountId);

    val dcSyncState = dbHelper.getAccount(accountId).getNylasSyncState();
    val modelSyncState = dbHelper.getAccountRepo().get(accountId).syncState();

    assertThat(dcSyncState).isNull();
    assertThat(modelSyncState).isEqualTo(SyncState.UNKNOWN);
  }

  @Test
  @SneakyThrows
  void shouldSetSyncStateToUnknownAndThrowNotFoundExceptionOnNylas404() {
    val accountId = dbHelper.createAccount(orgId);
    dbHelper.updateAccount(accountId, x -> x.setNylasSyncState(NylasAccountSyncState.running));

    when(getAppClientMock().accounts().get(accountId.value()))
        .thenThrow(new RequestFailedException(404, "message", "type"));

    assertThatCode(() -> runTask(accountId))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("account");

    val dcSyncState = dbHelper.getAccount(accountId).getNylasSyncState();
    val modelSyncState = dbHelper.getAccountRepo().get(accountId).syncState();

    assertThat(dcSyncState).isNull();
    assertThat(modelSyncState).isEqualTo(SyncState.UNKNOWN);
  }

  @Test
  @SneakyThrows
  void shouldSkipWhenRunningInNylasAndNotFoundLocally() {
    val accountId = TestData.accountId();
    val accountMock = NylasMockFactory.createAccountMock(accountId, "credentials");
    when(accountMock.getSyncState()).thenReturn("running");
    when(getAppClientMock().accounts().get(accountId.value())).thenReturn(accountMock);

    assertThatCode(() -> runTask(accountId))
        .doesNotThrowAnyException();
  }
}
