package com.UoU.core.nylas;

import com.nylas.Event;
import java.util.Optional;
import lombok.NonNull;

/**
 * Helper for interpreting the recurrence properties of a Nylas event.
 */
public class RecurrenceInfo {

  private final com.nylas.Event event;

  public RecurrenceInfo(@NonNull Event event) {
    this.event = event;
  }

  /**
   * Returns true if the event is a recurrence series master.
   */
  public boolean isMaster() {
    return event.getRecurrence() != null;
  }

  /**
   * Returns true if the event is a recurrence instance (has master event id).
   */
  public boolean isInstance() {
    return event.getMasterEventId() != null;
  }

  /**
   * Returns true if the event is an override instance (deviates from the master schedule).
   *
   * <p>Nylas does not have a specific property for overrides, but only overrides will have an
   * original_start_time so we can use the existence of that property to identify overrides.
   */
  public boolean isOverrideInstance() {
    return isInstance() && event.getOriginalStartTime() != null;
  }

  /**
   * Returns true if the event is a non-override instance (auto-generated from the master schedule).
   *
   * <p>Nylas does not have a specific property for overrides, but only overrides will have an
   * original_start_time so we can use the existence of that property to identify overrides.
   */
  public boolean isNonOverrideInstance() {
    return isInstance() && event.getOriginalStartTime() == null;
  }

  /**
   * Returns an optional of the master event id if the event is an instance.
   */
  public Optional<String> withInstanceMasterEventId() {
    return Optional.ofNullable(event.getMasterEventId());
  }

  @Override
  public String toString() {
    return "Recurrence["
        + withInstanceMasterEventId()
        .map(id -> "isInstance=true, isOverride=" + isOverrideInstance() + ", masterEventId=" + id)
        .or(() -> Optional
            .ofNullable(event.getRecurrence())
            .filter(x -> x.getRrule() != null)
            .map(x -> "isMaster=true, "
                + "rrule=[" + String.join(",", x.getRrule()) + "], "
                + "timezone=" + x.getTimezone()))
        .orElse("none")
        + "]";
  }
}
