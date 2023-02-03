package com.UoU._integration.core.nylas.tasks;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nylas.RequestFailedException;
import com.UoU._helpers.TestData;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;


public class DeleteAccountFromNylasTaskTests extends BaseNylasTaskTest {

  @Test
  @SneakyThrows
  void shouldWork() {
    val accountId = TestData.accountId();

    getNylasTaskRunnerSpy().deleteAccountFromNylas(accountId);
    verify(getAppClientMock().accounts()).delete(accountId.value());
  }

  @Test
  @SneakyThrows
  void shouldWorkOnNylas404() {
    val accountId = TestData.accountId();

    when(getAppClientMock().accounts().delete(accountId.value()))
        .thenThrow(new RequestFailedException(404, "message", "type"));

    getNylasTaskRunnerSpy().deleteAccountFromNylas(accountId);
    verify(getAppClientMock().accounts()).delete(accountId.value());
  }
}
