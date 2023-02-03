package com.UoU.core.calendars;

/**
 * Core internal calendar info, usually returned for new calendars so id and email are known.
 */
public record InternalCalendarInfo(
    CalendarId id,
    String name,
    String email
) {
}
