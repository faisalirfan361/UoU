package com.UoU.app.v1.dtos;

import com.UoU.app.docs.SchemaExt;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CalendarUpdateRequest", requiredProperties = SchemaExt.Required.ALL)
public record CalendarUpdateRequestDto(
    String name,
    @Schema(example = "America/Denver")
    String timezone
) {
}
