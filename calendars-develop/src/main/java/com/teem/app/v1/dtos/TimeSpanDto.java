package com.UoU.app.v1.dtos;

import com.UoU.app.docs.SchemaExt;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(
    name = "TimeSpan",
    example = "{\"start\": \"2022-02-24T10:00:00Z\", \"end\": \"2022-02-24T14:00:00Z\"}",
    requiredProperties = SchemaExt.Required.ALL)
public record TimeSpanDto(
    Instant start,
    Instant end
) {
}
