package com.UoU.infra.db.mapping;

/**
 * Common jooq mapping expressions.
 */
class Expressions {

  /**
   * Creates an OffsetDateTime for now at UTC.
   */
  public static final String NOW = "java(java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC))";

  /**
   * Id string of the default timezone from DataConfig.
   */
  public static final String DEFAULT_TIMEZONE_ID =
      "java(com.UoU.core.DataConfig.Calendars.DEFAULT_TIMEZONE.getId())";
}
