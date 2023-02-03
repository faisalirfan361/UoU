package com.UoU._integration.core;

import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.accounts.ServiceAccountRepository;
import com.UoU.core.auth.AuthService;
import com.UoU.core.calendars.CalendarRepository;
import com.UoU.core.nylas.tasks.NylasTaskScheduler;
import com.UoU.core.tasks.AdvanceEventsActivePeriodTask;
import com.UoU.core.tasks.BatchSpringTaskScheduler;
import com.UoU.core.tasks.TaskScheduler;
import com.UoU.core.tasks.UpdateExpiredServiceAccountRefreshTokensTask;
import com.UoU.core.tasks.UpdateServiceAccountRefreshTokenTask;
import lombok.AllArgsConstructor;
import lombok.val;

@AllArgsConstructor
public class TaskRunner implements TaskScheduler {
  private final ServiceAccountRepository serviceAccountRepo;
  private final CalendarRepository calendarRepo;
  private final AuthService authService;
  private final NylasTaskScheduler nylasTaskScheduler;
  private final BatchSpringTaskScheduler batchSpringTaskScheduler;

  @Override
  public void advanceEventsActivePeriod() {
    val task = new AdvanceEventsActivePeriodTask(
        calendarRepo, nylasTaskScheduler, batchSpringTaskScheduler);
    task.run();
  }

  @Override
  public void updateExpiredServiceAccountRefreshTokens() {
    val task = new UpdateExpiredServiceAccountRefreshTokensTask(
        serviceAccountRepo, this, batchSpringTaskScheduler);
    task.run();
  }

  @Override
  public void updateServiceAccountRefreshToken(ServiceAccountId id) {
    val task = new UpdateServiceAccountRefreshTokenTask(authService);
    task.run(new UpdateServiceAccountRefreshTokenTask.Params(id));
  }
}
