package com.UoU.app.v1.dtos;

import com.UoU.app.docs.SchemaExt;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "Calendar", requiredProperties = SchemaExt.Required.ALL)
public record CalendarDto(
    String id,
    @Schema(nullable = true) String accountId,
    String name,
    @Schema(nullable = true) String timezone,
    Instant createdAt,
    @Schema(nullable = true) Instant updatedAt
) {
}
