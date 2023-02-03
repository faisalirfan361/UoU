package com.UoU.app.v1.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(
    name = "FreeBusyDetailedResponse",
    description = "Calendar busy periods with extra event details, grouped by calendar id",
    example = "{\"itemsById\": {"
        + "\"calendarId1\": ["
        + "{\"start\": \"2022-02-17T18:45:00Z\", "
        + "\"end\": \"2022-02-17T19:00:00Z\", "
        + "\"eventId\": \"cbb9dc40-ac8e-489d-8270-abcc36df5ca5\", "
        + "\"eventTitle\": \"Example Event\"}"
        + "], \"calendarId2\": []}}")
public record FreeBusyDetailedResponseDto(
    @Schema(required = true) ItemsByIdDto<List<EventTimeSpanDto>> itemsById
) {
}
