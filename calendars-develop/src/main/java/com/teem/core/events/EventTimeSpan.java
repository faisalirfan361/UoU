package com.UoU.core.events;

import com.UoU.core.TimeSpan;
import java.util.UUID;
import javax.validation.constraints.NotNull;

/**
 * A period of time that represents an event, along with some core event details.
 *
 * <p>An event may have different representations of "when" (TimeSpan, DateSpan, Date), but this
 * is a view of the event with a TimeSpan that represents a particular span of UTC time. Events with
 * a DateSpan or Date require some interpretation to become a TimeSpan, but for some scenarios we
 * need to do this interpretation (for example, to calculate calendar availability).
 */
public record EventTimeSpan(
    @NotNull TimeSpan timeSpan,
    @NotNull UUID eventId,
    @NotNull String eventTitle
) {
}
