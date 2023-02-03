package com.UoU.core.events;

import com.UoU.core.TimeSpan;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("events")
public record EventsConfig(
    @NonNull EventsConfig.ActivePeriod activePeriod
) {

  /**
   * Active period for event sync, creation, availability checks, etc.
   *
   * <p>We store a limited amount of events, so events must fall within this active period.
   */
  public record ActivePeriod(
      int pastDays,
      int futureDays
  ) {

    public String description() {
      return "between " + pastDays + " days in the past and " + futureDays + " days in the future";
    }

    /**
     * Gets the current active period for right now.
     */
    public TimeSpan current() {
      return at(Instant.now());
    }

    /**
     * Gets the active period at a specific instant.
     */
    public TimeSpan at(Instant instant) {
      // Start = inclusive, truncated to start of day.
      // End = exclusive, truncated to start of next day.
      return new TimeSpan(
          instant.minus(Period.ofDays(pastDays)).truncatedTo(ChronoUnit.DAYS),
          instant.plus(Period.ofDays(futureDays + 1)).truncatedTo(ChronoUnit.DAYS));
    }
  }
}
