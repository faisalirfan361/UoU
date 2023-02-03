package com.UoU.app.v1.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(
    name = "FreeBusyResponse",
    description = "Calendar busy periods grouped by calendar id",
    example = "{\"itemsById\": {"
        + "\"calendarId1\": ["
        + "{\"start\": \"2022-02-17T18:45:00Z\", \"end\": \"2022-02-17T19:00:00Z\"}, "
        + "{\"start\": \"2022-02-17T19:15:00Z\", \"end\": \"2022-02-17T20:15:00Z\"}"
        + "], \"calendarId2\": []}}")
public record FreeBusyResponseDto(
    @Schema(required = true) ItemsByIdDto<List<TimeSpanDto>> itemsById
) {
}
