package com.UoU.app.v1.dtos;

import com.UoU.app.docs.SchemaExt;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(
    name = "RecurrenceInstance",
    description = "Recurring event instance info, including the id of the master event for "
        + "the series. This is only included for recurrence instance events.",
    requiredProperties = SchemaExt.Required.ALL)
public record RecurrenceInstanceDto(
    UUID masterId,
    boolean isOverride
) {
}
