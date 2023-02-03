package com.UoU.app.v1.dtos;

import static com.UoU.core.calendars.CalendarConstraints.NAME_MAX;
import static com.UoU.core.calendars.InternalCalendarBatchCreateRequest.NUMBER_TOKEN;

import com.UoU.app.docs.SchemaExt;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "InternalCalendarBatchCreateRequest",
    requiredProperties = { SchemaExt.Required.EXCEPT, "dryRun"})
public record InternalCalendarBatchCreateRequestDto(
    @Schema(example = "1") Integer start,
    @Schema(example = "3") Integer end,
    @Schema(example = "1") Integer increment,
    @Schema(maxLength = NAME_MAX, example = "Desk 3." + NUMBER_TOKEN) String namePattern,
    @Schema(example = "America/Denver") String timezone,
    @Schema(nullable = true, defaultValue = "false") Boolean dryRun
) {
}
