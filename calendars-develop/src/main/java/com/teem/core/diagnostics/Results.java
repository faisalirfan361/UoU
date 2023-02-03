package com.UoU.core.diagnostics;

import com.UoU.core.diagnostics.events.DiagnosticEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Results of a diagnostics run.
 */
public record Results(
    RunId runId,
    Status status,
    Instant startedAt,
    Instant finishedAt,
    Instant expiresAt,
    List<DiagnosticEvent> events
) {
  public Results {
    status = status != null ? status : Status.PENDING;
    events = events != null ? events : List.of();
  }

  public Optional<Duration> duration() {
    return Optional
        .ofNullable(startedAt)
        .flatMap(start -> Optional
            .ofNullable(finishedAt)
            .map(end -> Duration.between(start, end)));
  }
}
