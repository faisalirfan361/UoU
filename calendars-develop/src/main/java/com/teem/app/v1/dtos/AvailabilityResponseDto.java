package com.UoU.app.v1.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "AvailabilityResponse",
    description = "Calendar availability map. The keys are calendar ids, and the values are "
        + "availability (true/false).",
    example = "{\"itemsById\": {\"calendarId1\": true, \"calendarId2\": false}}")
public record AvailabilityResponseDto(
    @Schema(required = true) ItemsByIdDto<Boolean> itemsById
) {
}
