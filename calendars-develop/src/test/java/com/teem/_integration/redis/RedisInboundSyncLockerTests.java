package com.UoU._integration.redis;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.UoU._helpers.TestData;
import com.UoU._integration.BaseAppIntegrationTest;
import com.UoU.infra.redis.RedisInboundSyncLocker;
import java.time.Duration;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RedisInboundSyncLockerTests extends BaseAppIntegrationTest {

  @Autowired
  private RedisInboundSyncLocker locker;

  @Test
  void shouldLockAndUnlock() {
    val accountId = TestData.accountId();
    val lock = UUID.randomUUID();
    val ttl = Duration.ofMinutes(1);

    assertThat(locker.isAccountLocked(accountId))
        .as("Should be unlocked to start")
        .isFalse();

    assertThat(locker.lockAccount(accountId, ttl, lock))
        .as("Should lock first time")
        .isTrue();

    assertThat(locker.isAccountLocked(accountId))
        .as("Should now be locked first time")
        .isTrue();

    locker.unlockAccount(accountId, lock);
    assertThat(locker.isAccountLocked(accountId))
        .as("Should be unlocked after unlockAccount")
        .isFalse();

    assertThat(locker.lockAccount(accountId, ttl, lock))
        .as("Should lock second time")
        .isTrue();

    assertThat(locker.isAccountLocked(accountId))
        .as("Should now be locked second time")
        .isTrue();

    assertThat(locker.lockAccount(accountId, ttl, UUID.randomUUID()))
        .as("Should NOT lock with new lock id while already locked")
        .isFalse();

    assertThat(locker.lockAccount(accountId, ttl, lock, 3))
        .as("Should lock again with same lock id and a lock count of 3")
        .isTrue();

    assertThat(locker.isAccountLocked(accountId))
        .as("Should now be locked, still")
        .isTrue();

    // Unlock 3 times, should only be unlocked after 3 times because of lock count set above.
    locker.unlockAccount(accountId, lock);
    assertThat(locker.isAccountLocked(accountId)).isTrue();
    locker.unlockAccount(accountId, lock);
    assertThat(locker.isAccountLocked(accountId)).isTrue();
    locker.unlockAccount(accountId, lock);
    assertThat(locker.isAccountLocked(accountId)).isFalse();
  }

  @Test
  void unlockAccount_shouldDoNothingForNonCurrentLock() {
    val accountId = TestData.accountId();
    val lock = UUID.randomUUID();
    val invalidLock = UUID.randomUUID();

    locker.lockAccount(accountId, Duration.ofMinutes(1), lock);
    locker.unlockAccount(accountId, invalidLock);

    assertThat(locker.isAccountLocked(accountId)).isTrue();
    assertThat(locker.isAccountLocked(accountId, lock)).isFalse();
    assertThat(locker.isAccountLocked(accountId, invalidLock)).isTrue();
  }
}
