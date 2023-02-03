package com.UoU.core.nylas;

import com.UoU.core.accounts.AccountId;
import java.time.Duration;
import java.util.UUID;
import org.springframework.lang.Nullable;

/**
 * Locks inbound sync during sync operations that are sensitive to race conditions and high load.
 */
public interface InboundSyncLocker {

  /**
   * Locks the account with the given lock and returns true if the lock was obtained.
   *
   * <p>Obtain a lock before sync operations that are sensitive to having other inbound sync
   * operations occurring at the same time. If the lock is not obtained, some other lock exists and
   * so the calling code should not proceed.
   */
  default boolean lockAccount(AccountId accountId, Duration ttl, UUID lock) {
    return lockAccount(accountId, ttl, lock, 1);
  }

  /**
   * Locks the account with the given lock and count and returns true if the lock was obtained.
   *
   * <p>Obtain a lock before sync operations that are sensitive to having other inbound sync
   * operations occurring at the same time. If the lock is not obtained, some other lock exists and
   * so the calling code should not proceed.
   *
   * <p>The lockCount determines how many times {@link #unlockAccount(AccountId, UUID)} will have
   * to be called to decrement the lock and finally remove it. This is useful for an operation with
   * n child operations that will run asynchronously: in this case, each child operation can unlock,
   * and the lock will be fully removed once all child operations have run.
   */
  boolean lockAccount(AccountId accountId, Duration ttl, UUID lock, int lockCount);

  /**
   * Unlocks the account if the passed lock is the current lock, otherwise does nothing.
   *
   * <p>If the current lock has a count more than 1, this will decrement the count, and the lock
   * will only be fully removed once the count reaches 0.
   */
  void unlockAccount(AccountId accountId, UUID lock);

  /**
   * Returns whether the account has any current lock.
   *
   * <p>If the account is locked, be careful about what operations you run that may be sensitive to
   * issues from other inbound sync operations running at the same time.
   */
  default boolean isAccountLocked(AccountId accountId) {
    return isAccountLocked(accountId, null);
  }

  /**
   * Returns whether the account has any current lock, except for the allowedLock.
   *
   * <p>This is like {@link #isAccountLocked(AccountId)}, except allowedLock does not count as
   * locking the account. This is useful to check if code can proceed when a specific lock is
   * expected to have been obtained already.
   */
  boolean isAccountLocked(AccountId accountId, @Nullable UUID allowedLock);
}
