package com.UoU.core.nylas;

/**
 * Nylas values defined by the API contract but not suitably-typed in the SDK to check against.
 */
public class NylasValues {

  /**
   * Possible event status values (returned by SDK as raw String).
   *
   * <p>See https://developer.nylas.com/docs/api#get/events/id
   */
  public static class EventStatus {
    public static final String TENTATIVE = "tentative";
    public static final String CONFIRMED = "confirmed";
    public static final String CANCELLED = "cancelled";
  }
}
