package com.UoU.infra.redis;

import com.UoU.core.accounts.AccountId;
import com.UoU.core.nylas.InboundSyncLocker;
import com.UoU.infra.redis.scripts.LockWithCountScript;
import com.UoU.infra.redis.scripts.UnlockDecrCountScript;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * Redis implementation of the inbound sync locker.
 *
 * <p>This uses lua scripts (see scripts directory) so that lock/decr/get commands are atomic
 * and perform better than separate commands.
 */
@Service
@AllArgsConstructor
public class RedisInboundSyncLocker implements InboundSyncLocker {
  private final StringRedisTemplate redisTemplate;
  private final LockWithCountScript lockWithCountScript;
  private final UnlockDecrCountScript unlockDecrCountScript;

  @Override
  public boolean lockAccount(AccountId accountId, Duration ttl, UUID lock, int lockCount) {
    if (ttl.isZero() || ttl.isNegative()) {
      throw new IllegalArgumentException("Invalid ttl");
    }

    if (lockCount <= 0) {
      throw new IllegalArgumentException("Invalid lockCount");
    }

    return redisTemplate.execute(
        lockWithCountScript,
        List.of(Keys.currentLock(accountId), Keys.lockCount(accountId, lock)),
        lock.toString(),
        String.valueOf(lockCount),
        String.valueOf(ttl.toSeconds()));
  }

  @Override
  public void unlockAccount(AccountId accountId, UUID lock) {
    redisTemplate.execute(
        unlockDecrCountScript,
        List.of(Keys.currentLock(accountId), Keys.lockCount(accountId, lock)), lock.toString());
  }

  @Override
  public boolean isAccountLocked(AccountId accountId, @Nullable UUID allowedLock) {
    val current = redisTemplate.opsForValue().get(Keys.currentLock(accountId));
    return current != null && (allowedLock == null || !allowedLock.toString().equals(current));
  }

  private static class Keys {
    private static final String PREFIX = "inbound-sync-lock-";

    public static String currentLock(AccountId accountId) {
      return PREFIX + accountId.value();
    }

    public static String lockCount(AccountId accountId, UUID lock) {
      return PREFIX + accountId.value() + "-" + lock + "-count";
    }
  }
}
