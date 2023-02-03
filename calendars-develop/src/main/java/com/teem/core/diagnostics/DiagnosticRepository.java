package com.UoU.core.diagnostics;

import com.UoU.core.calendars.CalendarId;

public interface DiagnosticRepository {

  /**
   * Gets the current run id, or saves a new run id if none exists.
   */
  RunIdInfo getOrSaveCurrentRun(CalendarId calendarId);

  Status getStatus(RunId runId);

  Results getResults(RunId runId);

  void save(SaveRequest request);
}
