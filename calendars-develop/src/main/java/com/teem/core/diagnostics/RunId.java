package com.UoU.core.diagnostics;

import com.UoU.core.calendars.CalendarId;
import java.util.UUID;
import javax.validation.constraints.NotBlank;
import lombok.NonNull;

/**
 * Compound run id consisting of a calendar id and a UUID.
 */
public record RunId(
    @NonNull @NotBlank CalendarId calendarId,
    @NonNull UUID id
) {

  private static final String STRING_FORMAT = "RunId[calendarId=%s, id=%s]";

  @Override
  public String toString() {
    return String.format(STRING_FORMAT, calendarId.value(), id);
  }
}
