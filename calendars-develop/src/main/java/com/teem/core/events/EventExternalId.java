package com.UoU.core.events;

import com.UoU.core.WrappedValue;
import javax.validation.constraints.NotBlank;
import lombok.NonNull;

/**
 * The event id needed for external sync to correlate with the external (Nylas) event.
 */
public record EventExternalId(@NonNull @NotBlank String value) implements WrappedValue<String> {
}
