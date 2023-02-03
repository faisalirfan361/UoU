package com.UoU.core.diagnostics;

import java.util.Locale;

/**
 * Payload for a callback to an end user about a diagnostic run.
 */
public record Callback(
    String message,
    String calendarId,
    String runId,
    String status
) {
  public static final String EXAMPLE = "{"
      + "\"message\": \"string\", "
      + "\"calendarId\": \"string\", "
      + "\"runId\": \"string\", "
      + "\"status\": \"string\"}";

  public Callback(String message, RunId runId, Status status) {
    this(message,
        runId.calendarId().value(),
        runId.id().toString(),
        status.toString().toLowerCase(Locale.ROOT));
  }
}
