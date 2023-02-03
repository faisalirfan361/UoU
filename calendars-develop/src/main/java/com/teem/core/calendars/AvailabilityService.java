package com.UoU.core.calendars;

import com.UoU.core.TimeSpan;
import com.UoU.core.events.EventTimeSpan;
import com.UoU.core.events.EventsConfig;
import com.UoU.core.validation.ValidatorWrapper;
import java.util.List;
import java.util.Map;
import javax.validation.ValidationException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service for getting calendar availability.
 */
@Service
@AllArgsConstructor
public class AvailabilityService {
  private final AvailabilityRepository repo;
  private final ValidatorWrapper validator;
  private final EventsConfig eventsConfig;

  /**
   * Gets calendar availability (true/false).
   *
   * <p>Note that start times are inclusive and end times are exclusive, so events can abut the
   * request timespan without making the calendar unavailable.
   */
  public Map<CalendarId, Boolean> getAvailability(AvailabilityRequest request) {
    validator.validateAndThrow(request);
    validateActivePeriod(request.timeSpan());
    return repo.getAvailability(request);
  }

  /**
   * Gets calendar busy time periods.
   *
   * <p>Note that start times are inclusive and end times are exclusive, so events can abut the
   * request timespan without being considered a busy period.
   */
  public Map<CalendarId, List<TimeSpan>> getBusyPeriods(AvailabilityRequest request) {
    validator.validateAndThrow(request);
    validateActivePeriod(request.timeSpan());
    return repo.getBusyPeriods(request);
  }

  /**
   * Gets calendar busy time periods with some extra event details.
   *
   * <p>Note that start times are inclusive and end times are exclusive, so events can abut the
   * request timespan without being considered a busy period.
   */
  public Map<CalendarId, List<EventTimeSpan>> getDetailedBusyPeriods(AvailabilityRequest request) {
    validator.validateAndThrow(request);
    validateActivePeriod(request.timeSpan());
    return repo.getDetailedBusyPeriods(request);
  }

  private void validateActivePeriod(TimeSpan timeSpan) {
    if (!eventsConfig.activePeriod().current().contains(timeSpan)) {
      throw new ValidationException(
          "Time span must be within the current active period for events: "
          + eventsConfig.activePeriod().description());
    }
  }
}
