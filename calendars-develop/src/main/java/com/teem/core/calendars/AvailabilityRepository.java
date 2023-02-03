package com.UoU.core.calendars;

import com.UoU.core.TimeSpan;
import com.UoU.core.events.EventTimeSpan;
import java.util.List;
import java.util.Map;

public interface AvailabilityRepository {
  Map<CalendarId, Boolean> getAvailability(AvailabilityRequest request);

  Map<CalendarId, List<TimeSpan>> getBusyPeriods(AvailabilityRequest request);

  Map<CalendarId, List<EventTimeSpan>> getDetailedBusyPeriods(AvailabilityRequest request);
}
