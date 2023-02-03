package com.UoU.app.v1.dtos;

import com.UoU.core.events.EventConstraints;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "EventUpdateRequest")
public record EventUpdateRequestDto(
    @Schema(nullable = true, minLength = 1, maxLength = EventConstraints.TITLE_MAX) String title,
    @Schema(nullable = true, maxLength = EventConstraints.DESCRIPTION_MAX) String description,
    @Schema(nullable = true, maxLength = EventConstraints.LOCATION_MAX) String location,
    @Schema(required = true) WhenDto when,
    @Schema(nullable = true) RecurrenceDto recurrence,
    @Schema(nullable = true) boolean isBusy,
    @Schema(nullable = true) List<ParticipantDto> participants,
    @Schema(nullable = true, maxLength = EventConstraints.DATA_SOURCE_API_MAX, example = "mobile")
    String dataSource
) {
}
