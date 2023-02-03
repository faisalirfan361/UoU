package com.UoU.core.events;

import com.UoU.core.Checksum;
import com.UoU.core.OrgId;
import com.UoU.core.PageParams;
import com.UoU.core.calendars.CalendarId;
import java.time.Instant;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public record EventQuery(
    @NotNull @Valid OrgId orgId,
    @NotNull @Valid CalendarId calendarId,
    @Valid WhenQuery when,
    boolean expandRecurring,
    @Valid PageParams page
) {

  @lombok.Builder(builderClassName = "Builder")
  public EventQuery {
    page = page != null ? page : PageParams.DEFAULT;
  }

  /**
   * Returns a checksum of the filtering properties that affect which events are returned.
   */
  public String toFilterChecksum() {
    return new Checksum(
        orgId.value(),
        calendarId.value(),
        Optional.ofNullable(when).map(x -> x.toString()).orElse(null),
        Boolean.toString(expandRecurring))
        .getValue();
  }

  /**
   * Validates a previously-generated checksum to make sure the filtering properties match.
   */
  public void validateFilterChecksum(String checksum) {
    if (checksum == null || !checksum.equals(toFilterChecksum())) {
      throw new IllegalArgumentException("Invalid filter checksum for event query");
    }
  }

  public record WhenQuery(
      Instant startsBefore,
      Instant startsAfter,
      Instant endsBefore,
      Instant endsAfter
  ) {

    @lombok.Builder(builderClassName = "Builder")
    public WhenQuery {
    }
  }
}
