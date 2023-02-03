package com.UoU.core.calendars;

import com.UoU.core.WrappedValue;
import javax.validation.constraints.NotBlank;
import lombok.NonNull;

/**
 * The calendar id needed for external sync to correlate with the external (Nylas) calendar.
 */
public record CalendarExternalId(@NonNull @NotBlank String value) implements WrappedValue<String> {
}
