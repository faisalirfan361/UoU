package com.UoU.infra.kafka;

import static com.UoU.infra.avro.tasks.DiagnosticsAction.RUN_CALENDAR_SYNC_DIAGNOSTICS;

import com.UoU.core.diagnostics.RunId;
import com.UoU.core.diagnostics.tasks.TaskScheduler;
import com.UoU.infra.avro.tasks.Diagnostics;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
class DiagnosticTaskProducer implements TaskScheduler {
  private final Sender sender;
  private final TopicNames.Tasks topicNames;

  @Override
  public void runCalendarSyncDiagnostics(RunId runId, String callbackUri) {
    sender.send(
        topicNames.getDiagnostics(),
        new Diagnostics(
            runId.calendarId().value(),
            runId.id().toString(),
            callbackUri,
            RUN_CALENDAR_SYNC_DIAGNOSTICS));
  }
}
