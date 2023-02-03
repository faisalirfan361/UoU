package com.UoU.core.accounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU.core.OrgId;
import com.UoU.core.auth.AuthMethod;
import com.UoU.core.exceptions.IllegalOperationException;
import com.UoU.core.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;

public class ServiceAccountServiceTests {

  @Test
  void getById_shouldWork() {
    var scenario = new Scenario();
    var serviceAccount = scenario.service.get(scenario.orgId, scenario.id);
    assertThat(serviceAccount.id()).isEqualTo(scenario.id);
  }

  @Test
  void getById_shouldThrowForAccountInDifferentOrg() {
    var scenario = new Scenario();

    assertThatCode(() -> scenario.service.get(TestData.orgId(), scenario.id))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("ServiceAccount");
  }

  @Test
  void delete_shouldThrowForAccountInDifferentOrg() {
    var scenario = new Scenario();

    assertThatCode(() -> scenario.service.delete(TestData.orgId(), scenario.id))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("ServiceAccount");

    verify(scenario.deps.repoMock, never()).delete(any());
  }

  @Test
  void delete_shouldThrowWhenHasAccounts() {
    var scenario = new Scenario();
    when(scenario.deps.repoMock.hasAccounts(scenario.id)).thenReturn(true);

    assertThatCode(() -> scenario.service.delete(scenario.orgId, scenario.id))
        .isInstanceOf(IllegalOperationException.class)
        .hasMessageContaining("accounts");

    verify(scenario.deps.repoMock, never()).delete(any());
  }

  /**
   * Helper for setting up test scenarios for the service.
   */
  private static class Scenario {
    private final OrgId orgId = TestData.orgId();
    private final ServiceAccountId id = ServiceAccountId.create();
    private final String email = TestData.email();
    private final Dependencies deps = new Dependencies(
        mock(ServiceAccountRepository.class));
    private final ServiceAccountService service = new ServiceAccountService(
        deps.repoMock);

    public Scenario() {
      var serviceAccount = ModelBuilders
          .serviceAccount()
          .id(id)
          .orgId(orgId)
          .authMethod(AuthMethod.MS_OAUTH_SA)
          .email(email)
          .createdAt(TestData.instant())
          .build();
      when(deps.repoMock.getAccessInfo(id)).thenReturn(new ServiceAccountAccessInfo(orgId));
      when(deps.repoMock.get(id)).thenReturn(serviceAccount);
    }

    private record Dependencies(
        ServiceAccountRepository repoMock
    ) {
    }
  }
}
