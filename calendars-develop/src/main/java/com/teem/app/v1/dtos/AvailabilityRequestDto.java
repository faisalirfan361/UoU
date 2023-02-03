package com.UoU.app.v1.dtos;

import com.UoU.app.docs.SchemaExt;
import com.UoU.core.DataConfig;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(name = "AvailabilityRequest", requiredProperties = SchemaExt.Required.ALL)
public record AvailabilityRequestDto(
    @ArraySchema(minItems = 1, maxItems = DataConfig.Availability.MAX_CALENDARS)
    Set<String> calendarIds,

    TimeSpanDto timeSpan
) {
}
