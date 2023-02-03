package com.UoU.core.diagnostics.tasks;

import com.UoU.core.diagnostics.RunId;
import com.UoU.core.diagnostics.events.RunEvent;
import java.util.function.BiConsumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
class RunnerFactory {
  private org.springframework.scheduling.TaskScheduler springTaskScheduler;

  public Runner create(RunId runId, BiConsumer<RunEvent.ErrorOccurred, Throwable> onError) {
    return new Runner(springTaskScheduler, runId, onError);
  }
}
