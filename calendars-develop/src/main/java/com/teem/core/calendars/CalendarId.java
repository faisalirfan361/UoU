package com.UoU.core.calendars;

import com.UoU.core.WrappedValue;
import java.util.UUID;
import javax.validation.constraints.NotBlank;
import lombok.NonNull;

public record CalendarId(@NonNull @NotBlank String value) implements WrappedValue<String> {
  public static CalendarId create() {
    // TODO: Going forward, calendar ids will be UUIDs, since the external ids are stored separately
    // now. We need to initially support non-UUIDs for backward compatibility, but later we should
    // convert old ids to UUIDs and use a real UUID here and in the DB. This will break a few
    // people using the QA environment, but no-one in prod since this change is made pre-prod.
    return new CalendarId(UUID.randomUUID().toString());
  }
}
