package com.UoU.core.calendars;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("internal-calendars")
public record InternalCalendarsConfig(
    String emailSuffix
) {

  public InternalCalendarsConfig {
    if (emailSuffix == null || emailSuffix.isBlank()) {
      throw new IllegalArgumentException("Invalid internal calendars emailSuffix");
    }
  }

  /**
   * Gets the internal calendar email based on id and configured email suffix.
   */
  public String getEmail(CalendarId id) {
    return id.value() + emailSuffix;
  }
}
