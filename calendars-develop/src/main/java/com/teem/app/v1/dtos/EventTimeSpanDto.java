package com.UoU.app.v1.dtos;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.UoU.app.docs.SchemaExt;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(
    name = "EventTimeSpan",
    description = "A period of time that represents an event, along with some core event details",
    requiredProperties = SchemaExt.Required.ALL)
public record EventTimeSpanDto(
    @JsonUnwrapped TimeSpanDto timeSpan,
    UUID eventId,
    @Schema(nullable = true) String eventTitle
) {
}
