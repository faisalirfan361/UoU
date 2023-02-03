package com.UoU.core.tasks;

import java.time.Instant;
import java.util.Iterator;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Wraps the Spring TaskScheduler to make scheduling batches of tasks easier.
 *
 * <p>DO-LATER: If we end up implementing durable delayed tasks, we should switch this over, and
 * then probably spread the tasks/load out even more where it's used. For now, spring tasks are
 * good enough, since we only use it for very short delays and for tasks we can risk losing.
 */
@Service
@AllArgsConstructor
public class BatchSpringTaskScheduler {
  private final org.springframework.scheduling.TaskScheduler springTaskScheduler;

  /**
   * Schedules batches of tasks with Spring TaskScheduler, delaying each batch after the first.
   *
   * <p>Note that all tasks are sent to Spring TaskScheduler immediately but with different
   * scheduled run times for each batch. The first batch is scheduled for now, and subsequent
   * batches will have a delay. After this method returns, Spring TaskScheduler is responsible for
   * executing the tasks at the right time.
   *
   * <p>Spring TaskScheduler uses local threads for scheduling, so don't schedule anything too far
   * in advance or use this for scenarios where durability really matters. The main use case is to
   * spread operations out over seconds or minutes to avoid load spikes, where the risk of losing
   * the tasks is low.
   *
   * @param batches The batches to run.
   * @param delaySeconds The delay between the scheduled batches.
   * @param processor The processor for each batch when the scheduled task runs.
   * @param <T> The batch type.
   */
  public <T> void scheduleBatches(
      Iterator<T> batches, int delaySeconds, Consumer<T> processor) {

    if (delaySeconds <= 0) {
      throw new IllegalArgumentException(
          "delaySeconds must be greater than 0, else batches are unnecessary");
    }

    var batchIndex = 0;

    while (batches.hasNext()) {
      val batch = batches.next();
      springTaskScheduler.schedule(
          () -> processor.accept(batch),
          Instant.now().plusSeconds(batchIndex * delaySeconds));

      batchIndex++;
    }
  }
}
