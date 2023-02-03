package com.UoU.core.accounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU.core.OrgId;
import com.UoU.core.auth.AuthMethod;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.nylas.tasks.NylasTaskScheduler;
import java.util.stream.Stream;
import lombok.val;
import org.jooq.exception.NoDataFoundException;
import org.junit.jupiter.api.Test;

class AccountServiceTests {

  @Test
  void get_shouldWork() {
    val scenario = new Scenario();
    val account = scenario.service.get(scenario.orgId, scenario.accountId);
    assertThat(account.id()).isEqualTo(scenario.accountId);
  }

  @Test
  void get_shouldThrowForAccountInDifferentOrg() {
    val scenario = new Scenario();

    assertThatCode(() -> scenario.service.get(TestData.orgId(), scenario.accountId))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Account");
  }

  @Test
  void getWithoutOrgId_shouldWork() {
    val scenario = new Scenario();
    val account = scenario.service.get(scenario.accountId);
    assertThat(account.id()).isEqualTo(scenario.accountId);
  }

  @Test
  void listErrors_shouldWork() {
    val scenario = new Scenario().withAccountError();
    val errors = scenario.service.listErrors(scenario.orgId, scenario.accountId, true);
    assertThat(errors.findFirst().orElseThrow().accountId()).isEqualTo(scenario.accountId);
  }

  @Test
  void listError_shouldThrowForAccountInDifferentOrg() {
    val scenario = new Scenario().withAccountError();

    assertThatCode(() -> scenario.service.listErrors(TestData.orgId(), scenario.accountId, false))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Account");
  }

  @Test
  void delete_shouldDeleteFromNylas() {
    val scenario = new Scenario();

    assertThatCode(() -> scenario.service.delete(scenario.orgId, scenario.accountId))
        .doesNotThrowAnyException();

    verify(scenario.deps.accountRepoMock).delete(scenario.accountId);
    verify(scenario.deps.nylasTaskSchedulerMock)
        .deleteAccountFromNylas(scenario.accountId);
  }

  @Test
  void delete_shouldThrowForAccountInDifferentOrg() {
    val scenario = new Scenario();

    assertThatCode(() -> scenario.service.delete(TestData.orgId(), scenario.accountId))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Account");

    verify(scenario.deps.accountRepoMock, never()).delete(any());
    verifyNoInteractions(scenario.deps.nylasTaskSchedulerMock);
  }

  /**
   * Helper for setting up test scenarios for the service.
   */
  private static class Scenario {
    private final OrgId orgId = TestData.orgId();
    private final AccountId accountId = TestData.accountId();
    private ServiceAccountId serviceAccountId = TestData.serviceAccountId();
    private String email = TestData.email();
    private final Dependencies deps = new Dependencies(
        mock(AccountRepository.class),
        mock(NylasTaskScheduler.class));
    private final AccountService service = new AccountService(
        this.deps.accountRepoMock(),
        this.deps.nylasTaskSchedulerMock());

    public Scenario() {
      val account = ModelBuilders
          .account()
          .id(accountId)
          .serviceAccountId(serviceAccountId)
          .orgId(orgId)
          .email(email)
          .authMethod(AuthMethod.GOOGLE_OAUTH)
          .createdAt(TestData.instant())
          .build();
      when(deps.accountRepoMock.getAccessInfo(accountId)).thenReturn(new AccountAccessInfo(orgId));
      when(deps.accountRepoMock.get(accountId)).thenReturn(account);
      when(deps.accountRepoMock.get(email)).thenReturn(account);
      when(deps.accountRepoMock.get("newEmail@email.com")).thenThrow(NoDataFoundException.class);
    }

    public Scenario withAccountError() {
      when(deps.accountRepoMock.listErrors(eq(accountId), any(Boolean.class)))
          .thenReturn(
              Stream.of(new AccountError(accountId, AccountError.Type.AUTH, "msg", "details")));
      return this;
    }

    private record Dependencies(
        AccountRepository accountRepoMock,
        NylasTaskScheduler nylasTaskSchedulerMock) {
    }
  }
}
