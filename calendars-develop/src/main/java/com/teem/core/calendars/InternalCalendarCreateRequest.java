package com.UoU.core.calendars;

import com.UoU.core.OrgId;
import com.UoU.core.validation.annotations.TimeZone;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Request to create an internal calendar (which does not sync to MS/Google).
 */
public record InternalCalendarCreateRequest(
    @NotNull @Valid OrgId orgId,
    @NotBlank @Size(max = CalendarConstraints.NAME_MAX) String name,
    @NotNull @TimeZone String timezone
) {
}
