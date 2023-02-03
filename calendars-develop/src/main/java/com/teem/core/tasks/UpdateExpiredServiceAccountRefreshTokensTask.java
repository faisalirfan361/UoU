package com.UoU.core.tasks;

import com.UoU.core.Task;
import com.UoU.core.accounts.ServiceAccountRepository;
import com.UoU.core.auth.AuthMethod;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Updates all expired service account refresh tokens by scheduling a task for each one.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateExpiredServiceAccountRefreshTokensTask implements Task.WithNoParams {

  /**
   * Auth methods that use expiring refresh tokens that can be refreshed.
   */
  private static final Set<AuthMethod> AUTH_METHODS = Set.of(AuthMethod.MS_OAUTH_SA);

  private static final int BATCH_SIZE = 50;
  private static final int BATCH_DELAY_SECONDS = 5;

  private final ServiceAccountRepository serviceAccountRepo;
  private final TaskScheduler taskScheduler;
  private final BatchSpringTaskScheduler batchSpringTaskScheduler;

  @Override
  public void run() {
    // Get expired service account ids in batches and schedule updateServiceAccountRefreshToken in
    // batches with spring taskscheduler so that we don't overwhelm our db or send too many calls
    // to an OAuth provider at once. Normally, there shouldn't be too many service accounts anyway,
    // and often there will probably only be one batch. But if this task failed to run for a while,
    // there could be many service accounts. Note that spring taskscheduler just uses local threads,
    // so if the running node dies, the tasks will be lost. However, since we're not scheduling very
    // far in advance, and this should be run regularly, that's an ok risk for now.
    val batches = serviceAccountRepo.listExpiredSettings(AUTH_METHODS, BATCH_SIZE);
    batchSpringTaskScheduler.scheduleBatches(
        batches.iterator(),
        BATCH_DELAY_SECONDS,
        batch -> batch.forEach(taskScheduler::updateServiceAccountRefreshToken));
  }

}
