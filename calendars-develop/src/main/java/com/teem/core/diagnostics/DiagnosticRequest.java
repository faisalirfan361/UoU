package com.UoU.core.diagnostics;

import com.UoU.core.OrgId;
import com.UoU.core.calendars.CalendarId;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Request to start a new diagnostic run.
 */
public record DiagnosticRequest(
    @NotNull @Valid
    CalendarId calendarId,

    @NotNull @Valid
    OrgId orgId,

    @Pattern(regexp = "^https?://.+")
    @Size(max = DiagnosticConstraints.CALLBACK_URI_MAX)
    String callbackUri
) {
}
