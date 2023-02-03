package com.UoU.core.tasks;

import com.UoU.core.accounts.ServiceAccountId;

/**
 * Schedules core tasks to run asynchronously.
 */
public interface TaskScheduler {

  /**
   * Advances the events active period for all calendars that are eligible at the current time.
   */
  void advanceEventsActivePeriod();

  /**
   * Updates all expired service account refresh tokens by scheduling a task for each one.
   */
  void updateExpiredServiceAccountRefreshTokens();

  /**
   * Updates a single service account refresh token.
   */
  void updateServiceAccountRefreshToken(ServiceAccountId id);
}
