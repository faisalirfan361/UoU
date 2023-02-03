package com.UoU.infra.redis;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThatCode;
import static org.mockito.Mockito.mock;

import com.UoU._helpers.TestData;
import com.UoU.infra.redis.scripts.LockWithCountScript;
import com.UoU.infra.redis.scripts.UnlockDecrCountScript;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.redis.core.StringRedisTemplate;

class RedisInboundSyncLockerTests {
  private static final RedisInboundSyncLocker LOCKER = new RedisInboundSyncLocker(
      mock(StringRedisTemplate.class),
      mock(LockWithCountScript.class),
      mock(UnlockDecrCountScript.class));


  @ParameterizedTest
  @ValueSource(ints = { -1, 0 })
  void lockAccount_shouldThrowForInvalidTtl(int ttlSeconds) {
    assertThatCode(() -> LOCKER
        .lockAccount(TestData.accountId(), Duration.ofSeconds(ttlSeconds), UUID.randomUUID(), 1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("ttl");
  }

  @ParameterizedTest
  @ValueSource(ints = { -1, 0 })
  void lockAccount_shouldThrowForInvalidLockCount(int lockCount) {
    assertThatCode(() -> LOCKER
        .lockAccount(TestData.accountId(), Duration.ofSeconds(5), UUID.randomUUID(), lockCount))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("lockCount");
  }
}
