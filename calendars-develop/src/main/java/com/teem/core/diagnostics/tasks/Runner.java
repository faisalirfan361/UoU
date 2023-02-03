package com.UoU.core.diagnostics.tasks;

import com.UoU.core.diagnostics.RunId;
import com.UoU.core.diagnostics.events.RunEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.DelegatingErrorHandlingRunnable;
import org.springframework.util.concurrent.CompletableToListenableFutureAdapter;
import org.springframework.util.concurrent.ListenableFuture;

/**
 * Helper for running diagnostic operations and logging and handling errors.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
class Runner {
  private final TaskScheduler springTaskScheduler;
  private final RunId runId;
  private final BiConsumer<RunEvent.ErrorOccurred, Throwable> onError;

  /**
   * Runs the operation and handles any errors that occur.
   *
   * <p>Any exception will be caught, handled, passed to onError, and rethrown as a RunException.
   */
  public void run(String description, Runnable operation) {
    run(description, () -> {
      operation.run();
      return null; // return unused value to convert to Supplier
    });
  }

  /**
   * Runs the operation and handles any errors that occur.
   *
   * <p>Any exception will be caught, handled, passed to onError, and rethrown as a RunException.
   */
  public <T> T run(String description, Supplier<T> operation) {
    try {
      log.debug("Attempting: {} ({})", description, runId);
      return operation.get();
    } catch (Exception ex) {
      handleError(description, ex);
      throw new RunException(ex);
    }
  }


  /**
   * Runs the operation with a delay until the supplied value passes the matcher predicate.
   *
   * <p>Note that this relies on spring taskscheduler that uses local threads, so if the running
   * node dies, the tasks will be lost. However, since we're not scheduling very far in advance,
   * this should be an ok risk for now, especially since diagnostics are not critical.
   * DO-LATER: If we end up implementing durable delayed tasks, we should switch this over.
   */
  public <T> ListenableFuture<T> runAsyncUntilMatch(
      String description,
      Supplier<T> operation,
      Predicate<T> matcher,
      int maxAttempts,
      Duration delay) {

    if (maxAttempts < 1) {
      throw new IllegalArgumentException("Max attempts must be at least 1.");
    }

    if (delay.isNegative()) {
      throw new IllegalArgumentException("Delay cannot be negative.");
    }

    log.debug("Scheduling run async until match for: {} (attempts={}, delay={}, {})",
        description, maxAttempts, delay, runId);

    val future = new CompletableFuture<T>();
    runAsyncUntilMatchRecursive(description, operation, matcher, future, 1, maxAttempts, delay);
    return new CompletableToListenableFutureAdapter<>(future);
  }

  private <T> void runAsyncUntilMatchRecursive(
      String description,
      Supplier<T> operation,
      Predicate<T> matcher,
      CompletableFuture<T> future,
      int attempt,
      int maxAttempts,
      Duration delay) {

    if (attempt > maxAttempts) {
      throw new IllegalStateException("Exceeded max attempts for: " + description);
    }

    // Create a runnable with error handling delegated to handleError().
    Runnable runnable = new DelegatingErrorHandlingRunnable(() -> {
      log.debug("Run async until match {}/{} for: {} ({})",
          attempt, maxAttempts, description, runId);

      val value = operation.get();
      if (matcher.test(value)) {
        future.complete(value);
      } else {
        runAsyncUntilMatchRecursive(
            description, operation, matcher, future, attempt + 1, maxAttempts, delay);
      }
    }, ex -> {
      try {
        handleError(description, ex);
      } finally {
        future.completeExceptionally(new RunException(ex));
      }
    });

    springTaskScheduler.schedule(runnable, Instant.now().plus(delay));
  }

  private void handleError(String description, Throwable ex) {
    val errorId = UUID.randomUUID();

    // Log diagnostic error as info, not as error, so that we can find the message by errorId and
    // correlate with the diagnostic results. These errors represent failures in the diagnostic
    // run, but usually not actual problems in our code. We only want to log as errors things
    // that are problems with our code we need to address, not things that are expected to occur.
    log.info(
        "Diagnostic ERROR: {} (errorId={}) -- Attempting: {} ({})",
        ex.getMessage(), errorId, description, runId, ex);

    val event = new RunEvent.ErrorOccurred(
        "Error attempting: " + description,
        Map.of("errorId", errorId));

    try {
      onError.accept(event, ex);
    } catch (Exception onErrorEx) {
      // If the error handler itself causes another error, make sure we log it. But exclude
      // RunException because that means onError also used the runner to run() something, and
      // in that case, the error will be handled by the inner run() and probably isn't an
      // error in our code. We only want to log errors for exceptions that indicate bad code.
      if (!(onErrorEx instanceof RunException)) {
        log.error("Error in diagnostic onError handler for: {}", description, onErrorEx);
      }

      throw onErrorEx;
    }
  }

  /**
   * Any exception that occurs during the run of a diagnostic operation passed to the runner.
   */
  public static class RunException extends RuntimeException {
    public RunException(Throwable cause) {
      super(cause);
    }
  }
}
