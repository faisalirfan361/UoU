package com.UoU._integration.redis;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.UoU._helpers.ModelBuilders;
import com.UoU._integration.BaseAppIntegrationTest;
import lombok.val;
import org.junit.jupiter.api.Test;

public class RedisAuthCodeRepositoryTests extends BaseAppIntegrationTest {

  @Test
  void create_get_delete_shouldWork() {
    val repo = redisHelper.getAuthCodeRepo();
    val request = ModelBuilders.authCodeCreateRequestWithTestData().build();

    repo.create(request);
    val fetched = repo.tryGet(request.code());
    repo.tryDelete(request.code());
    val fetchedAfterDelete = repo.tryGet(request.code());

    assertThat(fetched).isPresent();
    assertThat(fetched.get().code()).isEqualTo(request.code());
    assertThat(fetchedAfterDelete).isEmpty();
  }
}
