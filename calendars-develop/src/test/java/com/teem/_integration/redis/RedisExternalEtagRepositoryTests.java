package com.UoU._integration.redis;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.UoU._helpers.TestData;
import com.UoU._integration.BaseAppIntegrationTest;
import com.UoU.core.nylas.ExternalEtag;
import com.UoU.infra.redis.RedisExternalEtagRepository;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;

public class RedisExternalEtagRepositoryTests extends BaseAppIntegrationTest {

  @Test
  void save_get_delete_shouldWork() {
    val externalId = TestData.eventExternalId();
    val etag = new ExternalEtag(TestData.uuidString());

    repo().save(externalId, etag);
    val result = repo().get(externalId);
    repo().tryDelete(externalId);

    assertThat(result).hasValue(etag);
    assertThat(repo().get(externalId)).isEmpty();
  }

  @Test
  void save_get_delete_shouldWorkWithMutliple() {
    val etags = Map.of(
        TestData.eventExternalId(), new ExternalEtag(TestData.uuidString()),
        TestData.eventExternalId(), new ExternalEtag(TestData.uuidString())
    );

    repo().save(etags);
    val result = repo().get(etags.keySet());
    repo().tryDelete(etags.keySet());

    assertThat(result).containsExactlyInAnyOrderEntriesOf(etags);
    assertThat(repo().get(etags.keySet())).isEmpty();
  }

  private RedisExternalEtagRepository repo() {
    return (RedisExternalEtagRepository) redisHelper.getExternalEtagRepo();
  }
}
