package com.UoU.core;

import com.UoU.core.accounts.Provider;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Set;

/**
 * Configuration for data limits and rules.
 *
 * <p>We could make some of this configurable via application settings if needed, eventually.
 * But since we're using annotations for doc generation and validation, and those annotations
 * require compile-time constants, this is way easier for some things now.
 */
public class DataConfig {
  /**
   * Config for paging.
   */
  public static class Paging {
    public static final int DEFAULT_LIMIT = 5;
    public static final int MAX_LIMIT = 100;

    // compile-time constants required for use in annotations:
    public static final String DEFAULT_LIMIT_STR = "5";
    public static final String MAX_LIMIT_STR = "100";
  }

  /**
   * Config for calendar availability endpoints.
   */
  public static class Availability {
    public static final int MAX_DURATION_DAYS = 10; // need a constant for annotations
    public static final Duration MAX_DURATION = Duration.ofDays(MAX_DURATION_DAYS);
    public static final int MAX_CALENDARS = 100;
  }

  public static class Auth {
    public static final int AUTH_CODE_EXPIRATION_MINUTES = 15;
  }

  public static class Accounts {
    public static final int MAX_ERRORS_PER_ACCOUNT = 10;
  }

  public static class Calendars {

    /**
     * Default timezone to use when a calendar does not have a timezone. We need calendar timezones
     * to interpret non-exact times to exact times (like for all-day events).
     */
    public static final ZoneId DEFAULT_TIMEZONE = ZoneId.of("UTC");

    /**
     * Providers that give us calendar timezones, which means API callers cannot set the timezones.
     */
    public static final Set<Provider> TIMEZONE_PROVIDERS = Set.of(Provider.GOOGLE);
  }
}
