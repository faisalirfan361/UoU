package com.UoU.core.events;

import com.UoU.core.OrgId;
import com.UoU.core.calendars.CalendarId;
import java.util.Optional;
import lombok.NonNull;

/**
 * Holder for core event ids.
 */
public record CoreIds(
    @NonNull EventId id,
    @NonNull Optional<EventExternalId> externalId,
    @NonNull CalendarId calendarId,
    @NonNull OrgId orgId
) {
}
