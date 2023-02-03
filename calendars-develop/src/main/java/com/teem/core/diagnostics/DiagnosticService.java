package com.UoU.core.diagnostics;

import com.UoU.core.calendars.CalendarRepository;
import com.UoU.core.diagnostics.tasks.TaskScheduler;
import com.UoU.core.validation.ValidatorWrapper;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DiagnosticService {
  private final DiagnosticRepository diagnosticRepo;
  private final CalendarRepository calendarRepo;
  private final ValidatorWrapper validator;
  private final TaskScheduler taskScheduler;

  /**
   * Starts a new diagnostics run, or if one is already running, returns the existing id.
   */
  public RunId run(DiagnosticRequest request) {
    validator.validateAndThrow(request);

    val calendar = calendarRepo.get(request.calendarId());
    calendar.getAccessInfo().requireOrgOrThrowNotFound(request.orgId());
    calendar.requireIsEligibleToSync();

    val runIdInfo = diagnosticRepo.getOrSaveCurrentRun(request.calendarId());
    if (runIdInfo.isNew()) {
      taskScheduler.runCalendarSyncDiagnostics(runIdInfo.runId(), request.callbackUri());
    }

    return runIdInfo.runId();
  }

  /**
   * Gets the results for a diagnostics run.
   */
  public Results getResults(RunId runId) {
    return diagnosticRepo.getResults(runId);
  }
}
