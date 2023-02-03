package com.UoU._fakes;

import com.UoU.core.Noop;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.tasks.TaskScheduler;

/**
 * A no-op implementation of the core scheduler for testing purposes.
 */
public class NoopTaskScheduler implements TaskScheduler {
  @Override
  public void advanceEventsActivePeriod() {
    Noop.because("this whole class is noop");
  }

  @Override
  public void updateExpiredServiceAccountRefreshTokens() {
    Noop.because("this whole class is noop");
  }

  @Override
  public void updateServiceAccountRefreshToken(ServiceAccountId id) {
    Noop.because("this whole class is noop");
  }
}
