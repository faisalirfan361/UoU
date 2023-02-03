package com.UoU.core.diagnostics.events;

import java.time.Instant;
import java.util.Map;

/**
 * An event that represents some diagnostic task completed or an error during diagnostics.
 */
public interface DiagnosticEvent {

  Instant getTime();

  String getMessage();

  Map<String, Object> getData();

  boolean isError();
}
