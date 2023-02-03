package com.UoU.core.diagnostics.tasks;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.UoU._helpers.TestData;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.diagnostics.RunId;
import com.UoU.core.diagnostics.events.RunEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;

class RunnerTests {
  private RunId runId;
  private BiConsumer onErrorMock;
  private Runner runner;
  private TaskScheduler taskSchedulerMock;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    runId = new RunId(CalendarId.create(), UUID.randomUUID());

    // Setup task scheduler mock that runs Runnable immediately.
    taskSchedulerMock = mock(TaskScheduler.class);
    when(taskSchedulerMock.schedule(any(Runnable.class), any(Instant.class))).then(inv -> {
      ((Runnable) inv.getArgument(0)).run();
      return null;
    });

    onErrorMock = mock(BiConsumer.class);
    runner = new Runner(taskSchedulerMock, runId, onErrorMock);
  }

  @Test
  void run_shouldRunPassedRunnable() {
    val runnableMock = mock(Runnable.class);
    runner.run("test", runnableMock);

    verify(runnableMock).run();
    verifyNoInteractions(onErrorMock);
  }

  @Test
  void run_shouldReturnSupplierValue() {
    val value = TestData.uuidString();
    val result = runner.run("test", () -> value);

    assertThat(result).isEqualTo(value);
    verifyNoInteractions(onErrorMock);
  }

  @Test
  @SuppressWarnings("unchecked")
  void run_shouldCallOnErrorAndWrapWithRunException() {
    val description = "Some test operation";
    val exception = new RuntimeException("test");
    Runnable runnable = () -> {
      throw exception;
    };

    assertThatCode(() -> runner.run(description, runnable))
        .isInstanceOf(Runner.RunException.class)
        .hasCause(exception);

    verify(onErrorMock).accept(
        argThat((RunEvent.ErrorOccurred x) -> x.getMessage().contains(description)),
        eq(exception));
  }

  @Test
  void runAsyncUntilMatch_shouldRequireValidAttempts() {
    assertThatCode(() -> runner.runAsyncUntilMatch(
        "test", () -> null, x -> true, 0, Duration.ofSeconds(1)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void runAsyncUntilMatch_shouldRequirePositiveDuration() {
    assertThatCode(() -> runner.runAsyncUntilMatch(
        "test", () -> null, x -> true, 5, Duration.ofSeconds(-1)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @SuppressWarnings("unchecked")
  @SneakyThrows
  void runAsyncUntilMatch_shouldCompleteFutureWithSuppliedValueOnMatch() {
    val description = TestData.uuidString();
    val value = TestData.uuidString();
    Supplier<String> operation = () -> value;
    Predicate<String> matcher = x -> true; // matches first time
    val attempts = 3;

    val future = runner.runAsyncUntilMatch(
        description, operation, matcher, attempts, Duration.ZERO);

    verify(taskSchedulerMock).schedule(any(Runnable.class), any(Instant.class));
    verifyNoInteractions(onErrorMock);
    assertThat(future.isDone()).isTrue();
    assertThat(future.get()).isEqualTo(value);
  }

  @Test
  @SuppressWarnings("unchecked")
  @SneakyThrows
  void runAsyncUntilMatch_shouldRunMaxAttemptsAndThenError() {
    val description = TestData.uuidString();
    val value = TestData.uuidString();
    Predicate<String> matcher = x -> false; // never matches
    val attempts = 3;

    val future = runner.runAsyncUntilMatch(
        description, () -> value, matcher, attempts, Duration.ZERO);

    verify(taskSchedulerMock, times(attempts)).schedule(any(Runnable.class), any(Instant.class));

    // Error handler should be called with original exception for max attempts exceeded:
    verify(onErrorMock).accept(
        argThat((RunEvent.ErrorOccurred x) -> x.getMessage().contains(description)),
        any(IllegalStateException.class));
    assertThat(future.isDone());

    // Runner throws a RunException, but the future adapter wraps that in ExecutionException:
    assertThatCode(() -> future.get())
        .isInstanceOf(ExecutionException.class)
        .hasCauseInstanceOf(Runner.RunException.class);
  }
}
