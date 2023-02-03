package com.UoU.core.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.val;
import org.junit.jupiter.api.Test;

class BatchSpringTaskSchedulerTests {

  @Test
  void scheduleBatches_shouldScheduleInOrderWithDelay() {
    // Mock a spring taskscheduler so that we can record the batches and times as they run.
    val runBatches = new ArrayList<Integer>();
    val runTimes = new ArrayList<Instant>();
    final Consumer<Integer> batchProcessor = runBatches::add;
    val springTaskSchedulerMock = mock(org.springframework.scheduling.TaskScheduler.class);
    when(springTaskSchedulerMock.schedule(any(Runnable.class), any(Instant.class))).then(inv -> {
      ((Runnable) inv.getArgument(0)).run(); // run so batchProcessor adds to runBatches
      runTimes.add(inv.getArgument(1));
      return null;
    });

    val scheduler = new BatchSpringTaskScheduler(springTaskSchedulerMock);
    val batches = List.of(1, 2, 3);
    val batchDelaySeconds = 60;

    val now = Instant.now();
    scheduler.scheduleBatches(batches.iterator(), batchDelaySeconds, batchProcessor);

    assertThat(runBatches)
        .as("Batches should have run in order")
        .containsExactlyElementsOf(batches);

    assertThat(runTimes.size())
        .as("Run time should have been recorded for each batch (if not, test code is wrong)")
        .isEqualTo(runBatches.size());

    assertThat(runTimes.get(0))
        .as("Batch 1 should have been scheduled for now")
        .isCloseTo(now, within(5, ChronoUnit.SECONDS));

    assertThat(runTimes.get(1))
        .as("Batch 2 should have been scheduled for +" + batchDelaySeconds)
        .isCloseTo(now.plusSeconds(batchDelaySeconds), within(5, ChronoUnit.SECONDS));

    assertThat(runTimes.get(2))
        .as("Batch 3 should have been scheduled for +" + batchDelaySeconds * 2)
        .isCloseTo(now.plusSeconds(batchDelaySeconds * 2), within(5, ChronoUnit.SECONDS));
  }
}
