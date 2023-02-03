package com.UoU.app.v1.dtos;

import com.UoU.app.docs.SchemaExt;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(
    name = "Recurrence",
    description = "Recurring event master info. Recurring instances are created according to the "
        + "RRULE and associated with the series master. This is only included for recurrence "
        + "master events.",
    requiredProperties = SchemaExt.Required.ALL)
public record RecurrenceDto(
    @Schema(example = "[\"RRULE:FREQ=DAILY;UNTIL=20220301T000000\"]")
    List<String> rrule,

    @Schema(example = "America/Denver")
    String timezone
) {
}
