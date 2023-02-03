package com.UoU.infra.kafka.consumers;

import com.UoU.infra.kafka.NoRetryException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.slf4j.Logger;
import org.springframework.lang.Nullable;

/**
 * Helps run consumer code with added logging and handling for retry/no-retry exceptions.
 */
// DO-LATER: Some of this error-handling and logging could be handled by a CommonErrorHandler on
// the spring kafka container. However, I couldn't get any modifications to the DefaultErrorHandler
// to work with our auto-configuration of the listener container. We might have to reconfig all that
// to get a CommonErrorHandler working.
@AllArgsConstructor
public class Runner {

  /**
   * Log instance that will be used for all logging.
   */
  private final Logger log;

  /**
   * Runner name that will be used for logging and exceptions, with any action names added to it.
   */
  private final String name;

  /**
   * Passes a Retry to the caller so that some code can be run with retry and other code not.
   *
   * <p>Any exception that occurs outside the `Retry.run()` will be wrapped in a NoRetryException so
   * that it's not retried. Any exception that occurs inside `Retry.run()` will be allowed to bubble
   * up so that it can potentially be retried. In both cases, the exception will be logged.
   *
   * <p>Note that running within the retry does not guarantee a retry. If the exception thrown is
   * a NoRetryException or other non-retryable exception, the action still won't be retried.
   *
   * <p>Examples:
   * <pre>{@code
   * // Exception that will *not* be retried:
   * runWithRetry(getActionName, retry -> { throw new Exception(); });
   *
   * // Exception that will bubble up and potentially be retried:
   * runWithRetry(getActionName, retry -> retry.run(() -> { throw new Exception(); }));
   * }</pre>
   *
   * @param actionName A description of the action for log messages and exceptions.
   * @param action The action to run.
   */
  @SneakyThrows
  public void runWithRetry(@Nullable Supplier<String> actionName, Consumer<Retry> action) {
    // Try to get full action name, and if that fails (should be rare), we don't want to retry.
    String fullName;
    try {
      fullName = name + Optional.ofNullable(actionName)
          .map(x -> x.get())
          .map(x -> "[" + x + "]")
          .orElse("");
    } catch (Exception ex) {
      throw new NoRetryException("Error getting action name for runWithRetry: " + name, ex);
    }

    log.debug("START consumer runWithRetry: {}", fullName);

    try {
      action.accept(new Retry());
    } catch (NoRetryException ex) {
      // If NoRetryException is explicitly thrown, just log and rethrow.
      // Handle first so NoRetryException isn't wrapped in another NoRetryException.
      log.error("Non-retryable exception occurred in {}: {}", fullName, ex.getMessage(), ex);
      throw ex;
    } catch (RetryException ex) {
      // Unwrap original exception and let it bubble up so retry can potentially happen.
      log.error(ex.getCause().getMessage(), ex.getCause());
      throw ex.getCause();
    } catch (Exception ex) {
      // Else wrap in a NoRetryException so no retry will happen.
      val msg = "Non-retryable exception in " + fullName + ": " + ex.getMessage();
      log.error(msg, ex);
      throw new NoRetryException(msg, ex);
    }

    log.debug("END consumer runWithRetry: {}", fullName);
  }

  /**
   * Shortcut for calling {@link #runWithRetry(Supplier, Consumer)} with no action name.
   *
   * <p>If the Runner already has a base name that makes sense, this overload is fine. But
   * sometimes, the Runner does multiple types of operations, so an extra action name helps.
   */
  @SneakyThrows
  public void runWithRetry(Consumer<Retry> action) {
    runWithRetry(null, action);
  }

  public static class Retry {

    /**
     * Runs an action that can potentially be retried (unless a non-retryable exception is thrown).
     */
    public void run(Runnable action) {
      try {
        action.run();
      } catch (NoRetryException ex) {
        // Don't wrap a NoRetryException because we already know it shouldn't be retried.
        throw ex;
      } catch (Exception ex) {
        throw new RetryException(ex);
      }
    }
  }

  private static class RetryException extends RuntimeException {
    public RetryException(Throwable cause) {
      super(cause);
    }
  }
}
