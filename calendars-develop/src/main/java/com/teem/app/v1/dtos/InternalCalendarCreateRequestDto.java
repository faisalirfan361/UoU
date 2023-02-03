package com.UoU.app.v1.dtos;

import com.UoU.app.docs.SchemaExt;
import com.UoU.core.calendars.CalendarConstraints;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "InternalCalendarCreateRequest", requiredProperties = SchemaExt.Required.ALL)
public record InternalCalendarCreateRequestDto(
    @Schema(maxLength = CalendarConstraints.NAME_MAX) String name,
    @Schema(example = "America/Denver") String timezone
) {
}
