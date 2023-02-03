package com.UoU.core.diagnostics.events;

import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Diagnostic events related to the run process/workflow itself.
 */
public interface RunEvent extends DiagnosticEvent {

  class RunStarted extends BaseEvent implements RunEvent {
    public RunStarted() {
      super("Diagnostic run started.");
    }
  }

  class RunSucceeded extends BaseEvent implements RunEvent {
    public RunSucceeded() {
      super("Diagnostic run succeeded.");
    }
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  class ErrorOccurred extends BaseEvent implements RunEvent {
    public ErrorOccurred(String message) {
      this(message, Map.of());
    }

    public ErrorOccurred(String message, Map<String, Object> data) {
      super(message, data, true);
    }
  }
}
