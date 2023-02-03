package com.UoU.core.calendars;

import com.UoU.core.DataConfig;
import com.UoU.core.OrgId;
import com.UoU.core.TimeSpan;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record AvailabilityRequest(
    @NotNull
    @Valid
    OrgId orgId,

    @NotNull
    @Size(min = 1, max = DataConfig.Availability.MAX_CALENDARS)
    Set<CalendarId> calendarIds,

    @NotNull
    @Valid
    TimeSpan timeSpan
) {

  public AvailabilityRequest {
    timeSpan = timeSpan == null
        ? null
        : timeSpan.withMaxDuration(DataConfig.Availability.MAX_DURATION);
  }

  public Set<String> calendarIdValues() {
    return calendarIds.stream().map(x -> x.value()).collect(Collectors.toSet());
  }
}
