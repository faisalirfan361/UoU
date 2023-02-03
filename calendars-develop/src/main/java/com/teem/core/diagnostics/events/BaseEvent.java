package com.UoU.core.diagnostics.events;

import java.time.Instant;
import java.util.Map;
import lombok.Getter;

/**
 * Base class for diagnostic events.
 *
 * <p>This is suitable for (de)serialization so that events can be saved directly to redis
 * or wherever without mapping to/from dtos. Generally, that means the class must have a
 * parameterless constructor and settable fields.
 */
@Getter
public abstract class BaseEvent implements DiagnosticEvent {
  private Instant time;
  private String message;
  private Map<String, Object> data;
  private boolean error;

  protected BaseEvent() { // for deserialization
  }

  protected BaseEvent(String message) {
    this(message, Map.of());
  }

  protected BaseEvent(String message, Map<String, Object> data) {
    this(message, data, false);
  }

  protected BaseEvent(String message, Map<String, Object> data, boolean isError) {
    this.time = Instant.now();
    this.message = message;
    this.data = data;
    this.error = isError;
  }
}
