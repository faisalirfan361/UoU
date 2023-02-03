package com.UoU.infra.kafka.consumers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.UoU._helpers.TestData;
import com.UoU.infra.kafka.NoRetryException;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

class RunnerTests {
  private Logger log;
  private Runner runner;
  private RuntimeException exception;

  @BeforeEach
  void setUp() {
    log = mock(Logger.class);
    runner = new Runner(log, "test");
    exception = new RuntimeException("Test " + TestData.uuidString());
  }

  @Test
  void runWithRetry_shouldUseActionNameInLogsAndException() {
    val actionName = TestData.uuidString();
    assertThatCode(() -> runner.runWithRetry(() -> actionName, retry -> doThrow(exception)))
        .isInstanceOf(NoRetryException.class)
        .hasCauseReference(exception)
        .hasMessageContaining(actionName);

    verify(log).debug(anyString(), contains(actionName));
    verify(log).error(contains(actionName), eq(exception));
  }

  @Test
  void runWithRetry_shouldThrowNoRetryExceptionForActionNameException() {
    assertThatCode(() -> runner.runWithRetry(
        () -> doThrowString(exception), retry -> doThrow(new RuntimeException())))
        .isInstanceOf(NoRetryException.class)
        .hasCauseReference(exception)
        .hasMessageContaining("action name");
  }

  @Test
  void runWithRetry_shouldThrowNoRetryExceptionWhenOutsideRetry() {
    assertThatCode(() -> runner.runWithRetry(retry -> doThrow(exception)))
        .isInstanceOf(NoRetryException.class)
        .hasCauseReference(exception);

    // Original exception should be logged:
    verify(log).error(contains("Non-retryable"), eq(exception));
  }

  @Test
  void runWithRetry_shouldThrowOriginalExceptionWhenInsideRetry() {
    assertThatCode(() -> runner.runWithRetry(retry -> retry.run(() -> doThrow(exception))))
        .isEqualTo(exception);

    // Original exception should be logged:
    verify(log).error(eq(exception.getMessage()), eq(exception));
  }

  @Test
  void runWithRetry_shouldNotWrapNoRetryException() {
    val noRetryException = new NoRetryException(exception);
    assertThatCode(() -> runner.runWithRetry(retry -> doThrow(noRetryException)))
        .isInstanceOf(NoRetryException.class)
        .hasCauseReference(exception);

    // Original NoRetryException should be logged:
    verify(log).error(contains("Non-retryable"), any(), any(), eq(noRetryException));
  }

  @Test
  void runWithRetry_shouldNotWrapNoRetryExceptionInsideRetry() {
    val noRetryException = new NoRetryException("DO_NOT_RETRY", exception);
    assertThatCode(() -> runner.runWithRetry(retry -> retry.run(() -> doThrow(noRetryException))))
        .isInstanceOf(NoRetryException.class)
        .hasCauseReference(exception);

    // Original NoRetryException should be logged:
    verify(log).error(contains("Non-retryable"), any(), any(), eq(noRetryException));
  }

  private static void doThrow(RuntimeException ex) {
    throw ex;
  }

  private static String doThrowString(RuntimeException ex) {
    throw ex;
  }
}
