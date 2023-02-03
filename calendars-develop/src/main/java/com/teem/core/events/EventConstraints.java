package com.UoU.core.events;

/**
 * Public event-related constraints that can be used outside this package, like in docs.
 */
public class EventConstraints {
  public static final int TITLE_MAX = 1024;
  public static final int DESCRIPTION_MAX = 8192;
  public static final int LOCATION_MAX = 255;
  public static final int PARTICIPANT_NAME_MAX = 250;
  public static final int PARTICIPANT_COMMENT_MAX = 1000;
  public static final int DATA_SOURCE_API_MAX = DataSource.MAX_LENGTH_API;
}
