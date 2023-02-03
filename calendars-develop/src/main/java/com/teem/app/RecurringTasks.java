package com.UoU.app;

import com.UoU.core.Noop;
import com.UoU.core.tasks.TaskScheduler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Configures recurring tasks when `recurring-tasks.enabled` is true.
 *
 * <p>Recurring tasks should be lightweight with all real processing taking place somewhere else.
 * Usually, recurring tasks should produce kafka messages and let consumers do any heavy lifting.
 *
 * <p>The tasks schedules are configured via `recurring-tasks.tasks` using cron expressions. The
 * cron expressions are handled by {@link org.springframework.scheduling.support.CronExpression},
 * which extends standard cron expressions to support seconds and a few other things. This format
 * also supports disabling a cron by setting the expression to "-" (hyphen).
 * See https://spring.io/blog/2020/11/10/new-in-spring-5-3-improved-cron-expressions
 *
 * <p>Actual code execution and threading is handled by the Spring TaskScheduler, which can be
 * configured via `spring.task.scheduling` to adjust thread pool size, termination period, etc.
 * Spring's TaskScheduler must be enabled (@EnableScheduling) for this to work.
 *
 * <p>Task locking is done via Shedlock and Redis to ensure recurring tasks are safe to run on many
 * nodes in a clustered environment. (Note: If we ever end up making a separate entrypoint for just
 * recurring tasks, we could probably just run one node and remove the locking. And if we end up
 * needing more advanced state tracking, not just locking, we could use Quartz instead of Shedlock.)
 */
@Configuration
@ConditionalOnProperty("recurring-tasks.enabled")
@EnableSchedulerLock(
    defaultLockAtLeastFor = "${recurring-tasks.locking.lock-at-least-for}",
    defaultLockAtMostFor = "${recurring-tasks.locking.lock-at-most-for}")
@Slf4j
@AllArgsConstructor
class RecurringTasks {
  private final TaskScheduler taskScheduler;

  /**
   * Recurring task: Logs heartbeat to indicate that recurring task system is working.
   */
  @Scheduled(
      cron = "${recurring-tasks.tasks.heartbeat.cron}",
      zone = "${recurring-tasks.tasks.heartbeat.zone}")
  @SchedulerLock(name = "heartbeat")
  void heartbeat() {
    run("heartbeat", () -> Noop.because("standard task logging is enough"));
  }

  /**
   * Recurring task: Advance the active period for all calendars eligible at current midnight.
   *
   * <p>Schedule just past each hour so this is sure to run at each local hour 0 (midnight) in case
   * the db clock is slightly behind the scheduler clock.
   */
  @Scheduled(
      cron = "${recurring-tasks.tasks.advance-events-active-period.cron}",
      zone = "${recurring-tasks.tasks.advance-events-active-period.zone}")
  @SchedulerLock(name = "advance-events-active-period")
  void advanceEventsActivePeriod() {
    run("advance-events-active-period", taskScheduler::advanceEventsActivePeriod);
  }

  /**
   * Recurring task: Update all expired service account refresh tokens.
   *
   * <p>Service account setting expiration should be set to allow ample time for the refresh to
   * happen, so timing of this task is not critical. It can usually be done once per night during
   * off hours, or maybe once per hour at most.
   */
  @Scheduled(
      cron = "${recurring-tasks.tasks.update-expired-service-account-refresh-tokens.cron}",
      zone = "${recurring-tasks.tasks.update-expired-service-account-refresh-tokens.zone}")
  @SchedulerLock(name = "update-expired-service-account-refresh-tokens")
  void updateExpiredServiceAccountRefreshTokens() {
    run("update-expired-service-account-refresh-tokens",
        taskScheduler::updateExpiredServiceAccountRefreshTokens);
  }

  /**
   * Helper that runs a task runnable with exception handling and standard logging.
   */
  private static void run(String taskName, Runnable task) {
    log.info("Recurring task STARTED: {}", taskName);

    try {
      task.run();
    } catch (Exception ex) {
      log.error("Recurring task FAILED: {}", taskName, ex);
      throw ex;
    }

    // Usually, the STARTED message is enough, but for debug also log FINISHED:
    log.debug("Recurring task FINISHED: {}", taskName);
  }

  /**
   * Creates a locker via Shedlock and Redis to ensure tasks only run once in a clustered env.
   */
  @Bean
  LockProvider lockProvider(
      RedisConnectionFactory connectionFactory,
      @Value("${recurring-tasks.locking.namespace}") String lockNamespace) {
    return new RedisLockProvider(connectionFactory, lockNamespace);
  }
}
