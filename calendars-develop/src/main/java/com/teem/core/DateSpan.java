package com.UoU.core;

import java.time.LocalDate;
import javax.validation.constraints.NotNull;

/**
 * General purpose representation of a span of time between two dates without time or timezones.
 */
public record DateSpan(
    @NotNull LocalDate start,
    @NotNull LocalDate end) {
}
