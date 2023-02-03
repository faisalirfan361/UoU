package com.UoU.core.diagnostics.tasks;

import com.UoU.core.diagnostics.RunId;

/**
 * Schedules diagnostics tasks.
 */
public interface TaskScheduler {
  void runCalendarSyncDiagnostics(RunId runId, String callbackUri);
}
