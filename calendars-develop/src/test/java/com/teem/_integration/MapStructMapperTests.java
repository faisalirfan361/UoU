package com.UoU._integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.UoU._helpers.ModelBuilders;
import com.UoU.app.v1.mapping.AccountMapper;
import com.UoU.core.OrgId;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.auth.AuthMethod;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Do some basic mapstruct testing as a sanity check. Testing every mapper seems unnecessary,
 * but we want to ensure spring DI and basic mapping works in case something breaks it.
 *
 * <p>If there is a particular, tricky mapper you want to test, make a unit test
 * specifically for that mapper. It probably doesn't need to be an integration test at all.
 */
class MapStructMapperTests extends BaseAppIntegrationTest {

  @Autowired
  private AccountMapper accountMapper;

  @Test
  void mapper_shouldWork() {
    var model = ModelBuilders.account()
        .id(new AccountId("id"))
        .serviceAccountId(new ServiceAccountId(UUID.randomUUID()))
        .orgId(new OrgId("org"))
        .email("email")
        .name("Test Account")
        .authMethod(AuthMethod.MS_OAUTH_SA)
        .createdAt(Instant.now())
        .build();
    var dto = accountMapper.toDto(model);

    assertThat(dto.serviceAccountId()).isEqualTo(model.serviceAccountId().value());
  }
}
