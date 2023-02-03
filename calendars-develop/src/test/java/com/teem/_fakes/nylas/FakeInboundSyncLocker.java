package com.UoU._fakes.nylas;

import com.UoU.core.Noop;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.nylas.InboundSyncLocker;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FakeInboundSyncLocker implements InboundSyncLocker {

  private static final Map<AccountId, Boolean> LOCK_RESULTS = new HashMap<>();
  private static final Map<AccountId, Boolean> IS_LOCKED_RESULTS = new HashMap<>();

  @Override
  public boolean lockAccount(AccountId accountId, Duration ttl, UUID lock, int lockCount) {
    return LOCK_RESULTS.getOrDefault(accountId, true);
  }

  @Override
  public void unlockAccount(AccountId accountId, UUID lock) {
    Noop.because("fake");
  }

  @Override
  public boolean isAccountLocked(AccountId accountId, UUID allowedLock) {
    return IS_LOCKED_RESULTS.getOrDefault(accountId, false);
  }

  public static void fakeLockAccountResult(AccountId accountId, boolean result) {
    LOCK_RESULTS.put(accountId, result);
  }

  public static void fakeIsAccountLockedResult(AccountId accountId, boolean result) {
    IS_LOCKED_RESULTS.put(accountId, result);
  }
}
