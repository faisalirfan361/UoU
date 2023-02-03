package com.UoU.core.diagnostics;

import com.UoU.core.diagnostics.events.DiagnosticEvent;
import java.time.Instant;
import java.util.List;
import lombok.NonNull;
import lombok.Singular;

/**
 * A request to save run info, where only set/non-null fields will be saved.
 */
public record SaveRequest(
    @NonNull RunId runId,
    Status status,
    Instant startedAt,
    Instant finishedAt,
    List<DiagnosticEvent> newEvents
) {

  public SaveRequest {
    newEvents = newEvents != null ? newEvents : List.of();
  }

  /**
   * Builder method to workaround intellij not recognizing @Singular on records.
   *
   * <p>Lombok supports @Builder and @Singular on records, but intellij doesn't recognize it. If
   * the issue is fixed, this could be removed, and @Builder could be moved to the record itself.
   * See https://youtrack.jetbrains.com/issue/IDEA-266513
   */
  @lombok.Builder(builderClassName = "Builder")
  private static SaveRequest create(
      @NonNull RunId runId,
      Status status,
      Instant startedAt,
      Instant finishedAt,
      @Singular List<DiagnosticEvent> newEvents) {
    return new SaveRequest(runId, status, startedAt, finishedAt, newEvents);
  }
}
